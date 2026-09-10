<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as THREE from 'three'
import {
  clearPulses,
  prunePulses,
  samplePulses,
  spawnPulseFromNdc,
  syncWaveSpace,
} from '@/components/auth/loginWave'

const props = defineProps<{
  sceneKey: string
}>()

const hostRef = ref<HTMLDivElement | null>(null)

const CELL = 0.168
const MAX_COLS = 128
const MAX_ROWS = 80
const MAX_COUNT = MAX_COLS * MAX_ROWS
const MAX_SPARKS = 8000
const SPARKS_PER = 22
const POINT_TOTAL = MAX_COUNT + MAX_SPARKS
const MAX_SEGMENTS = (MAX_COLS - 1) * MAX_ROWS + MAX_COLS * (MAX_ROWS - 1)

const PALETTES: Record<string, [string, string, string]> = {
  journal: ['#d2e3fc', '#8ab4f8', '#fde293'],
  health: ['#ceead6', '#81c995', '#fde293'],
  rhythm: ['#fde293', '#fbbc05', '#f6aea9'],
  voyage: ['#fdba74', '#f9a8d4', '#c4b5fd'],
  vault: ['#c7d2fe', '#a5b4fc', '#a5f3fc'],
}

let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.OrthographicCamera | null = null
let points: THREE.Points | null = null
let lines: THREE.LineSegments | null = null
let wash: THREE.Mesh | null = null
let washMat: THREE.ShaderMaterial | null = null
let raf = 0
let running = false
let cols = 48
let rows = 28
let count = cols * rows
let originX = 0
let originY = 0
let cell = CELL
let brush = CELL * 2.2
let spanX = 16
let spanY = 9

const mouse = new THREE.Vector2(0, 0)
const mousePrev = new THREE.Vector2(0, 0)
const mouseVel = new THREE.Vector2(0, 0)
const colorA = new THREE.Color()
const colorB = new THREE.Color()
const colorC = new THREE.Color()
const colorTargetA = new THREE.Color()
const colorTargetB = new THREE.Color()
const colorTargetC = new THREE.Color()
const tmpColor = new THREE.Color()
const white = new THREE.Color('#ffffff')
const gridTint = new THREE.Color('#f8fbff')

const pos = new Float32Array(POINT_TOTAL * 3)
const dirX = new Float32Array(POINT_TOTAL)
const dirY = new Float32Array(POINT_TOTAL)
const age = new Float32Array(POINT_TOTAL)
const speed = new Float32Array(POINT_TOTAL)
const grain = new Float32Array(POINT_TOTAL)
const rebuild = new Float32Array(MAX_COUNT)
const hold = new Float32Array(MAX_COUNT)
const phase = new Float32Array(POINT_TOTAL)
const waveAmt = new Float32Array(MAX_COUNT)
let sparkCursor = 0
const AGE_STEP = 1 / 96
const MOVE = 0.0145
const PIXEL = { value: 1 }

let posAttr: THREE.BufferAttribute | null = null
let sizeAttr: THREE.BufferAttribute | null = null
let colorAttr: THREE.BufferAttribute | null = null
let pointAlpha: THREE.BufferAttribute | null = null
let linePos: THREE.BufferAttribute | null = null
let lineAlpha: THREE.BufferAttribute | null = null
let lineColor: THREE.BufferAttribute | null = null

const pointVert = `
uniform float uPixelRatio;
attribute float aSize;
attribute float aAlpha;
attribute vec3 aColor;
varying vec3 vColor;
varying float vAlpha;
void main() {
  vColor = aColor;
  vAlpha = aAlpha;
  vec4 mv = modelViewMatrix * vec4(position, 1.0);
  gl_PointSize = max(1.8, aSize * uPixelRatio);
  gl_Position = projectionMatrix * mv;
}
`
const pointFrag = `
precision highp float;
varying vec3 vColor;
varying float vAlpha;
void main() {
  vec2 p = gl_PointCoord * 2.0 - 1.0;
  float d = length(p);
  if (d > 1.0) discard;
  float core = exp(-d * d * 5.2);
  gl_FragColor = vec4(vColor, core * vAlpha);
}
`
const lineVert = `
attribute vec3 aColor;
attribute float aAlpha;
varying vec3 vColor;
varying float vAlpha;
void main() {
  vColor = aColor;
  vAlpha = aAlpha;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
`
const lineFrag = `
precision highp float;
varying vec3 vColor;
varying float vAlpha;
void main() {
  if (vAlpha < 0.01) discard;
  gl_FragColor = vec4(vColor, vAlpha);
}
`
const washVert = `
varying vec2 vUv;
void main() {
  vUv = uv;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
`
const washFrag = `
precision highp float;
uniform float uTime;
uniform vec2 uMouse;
uniform vec3 uA;
uniform vec3 uB;
uniform vec3 uC;
varying vec2 vUv;
void main() {
  vec2 uv = vUv;
  float t = uTime;
  float n = 0.0;
  n += sin(uv.x * 4.2 + uv.y * 1.6 + t * 0.22);
  n += sin(uv.x * -2.8 + uv.y * 3.4 + t * 0.16) * 0.7;
  n += sin((uv.x + uv.y) * 6.0 - t * 0.12) * 0.35;
  n = n * 0.18 + 0.5;
  vec2 flow = uv + vec2(sin(t * 0.07), cos(t * 0.05)) * 0.04;
  float band = smoothstep(0.0, 1.0, flow.x * 0.6 + n * 0.4);
  vec3 col = mix(uA, uB, band);
  col = mix(col, uC, 0.18 + 0.18 * sin(t * 0.2 + uv.y * 3.0));
  vec2 m = uMouse * 0.5 + 0.5;
  float d = length(uv - m);
  col += uC * exp(-d * 5.5) * 0.22;
  float alpha = 0.14 + n * 0.08;
  gl_FragColor = vec4(col, alpha);
}
`

function applyPalette(key: string, instant = false) {
  const [a, b, c] = PALETTES[key] || PALETTES.journal
  colorTargetA.set(a)
  colorTargetB.set(b)
  colorTargetC.set(c)
  if (instant) {
    colorA.copy(colorTargetA)
    colorB.copy(colorTargetB)
    colorC.copy(colorTargetC)
  }
}

function idx(x: number, y: number) {
  return y * cols + x
}

function emitOne(x: number, y: number, ang: number, size: number) {
  const i = MAX_COUNT + sparkCursor
  sparkCursor = (sparkCursor + 1) % MAX_SPARKS
  dirX[i] = Math.cos(ang)
  dirY[i] = Math.sin(ang)
  speed[i] = MOVE
  grain[i] = size
  age[i] = 0.001
  const i3 = i * 3
  pos[i3] = x
  pos[i3 + 1] = y
  pos[i3 + 2] = 0
}

function emitSparks(x: number, y: number, baseAng: number) {
  for (let k = 0; k < SPARKS_PER; k++) {
    const ang = (k / SPARKS_PER) * Math.PI * 2 + (Math.random() - 0.5) * 0.18
    const spread = (Math.random() - 0.5) * cell
    emitOne(
      x + Math.cos(ang) * spread * 0.35,
      y + Math.sin(ang) * spread * 0.35,
      baseAng * 0.25 + ang,
      2.6 + Math.random() * 1.6,
    )
  }
  for (let k = 0; k < 8; k++) {
    const u = (k + 0.5) / 8
    emitOne(x + (u - 0.5) * cell, y, baseAng + (Math.random() - 0.5) * 0.4, 2.2 + Math.random() * 1.2)
    emitOne(x, y + (u - 0.5) * cell, baseAng + (Math.random() - 0.5) * 0.4, 2.2 + Math.random() * 1.2)
  }
}

function seedLattice() {
  sparkCursor = 0
  for (let i = 0; i < POINT_TOTAL; i++) {
    age[i] = 0
    dirX[i] = 0
    dirY[i] = 0
    speed[i] = 0
    grain[i] = 0
    if (i < MAX_COUNT) {
      rebuild[i] = 1
      hold[i] = 0
    }
    if (i < count) phase[i] = Math.random() * Math.PI * 2
  }
}

function homeOf(i: number, t: number, out: { x: number; y: number; z: number }) {
  const x = i % cols
  const y = (i - x) / cols
  const w = samplePulses(x, y, t)
  out.x = originX + x * cell + w.x
  out.y = originY + y * cell + w.y
  out.z = 0
  return w.crest
}

function writeLines() {
  if (!linePos || !lineAlpha || !lineColor) return
  const lp = linePos.array as Float32Array
  const la = lineAlpha.array as Float32Array
  const lc = lineColor.array as Float32Array
  let w = 0
  let a = 0
  let c = 0
  const emit = (i: number, j: number) => {
    const i3 = i * 3
    const j3 = j * 3
    lp[w++] = pos[i3]
    lp[w++] = pos[i3 + 1]
    lp[w++] = pos[i3 + 2]
    lp[w++] = pos[j3]
    lp[w++] = pos[j3 + 1]
    lp[w++] = pos[j3 + 2]
    const flying = (age[i] > 0 && age[i] < 1) || (age[j] > 0 && age[j] < 1)
    const mesh = Math.min(rebuild[i], rebuild[j])
    const crest = Math.max(waveAmt[i], waveAmt[j])
    const alpha = flying ? 0 : mesh * (0.1 + crest * 0.34)
    la[a++] = alpha
    la[a++] = alpha
    tmpColor.copy(gridTint).lerp(colorA, 0.08 + crest * 0.42)
    tmpColor.lerp(white, crest * 0.22)
    for (let k = 0; k < 2; k++) {
      lc[c++] = tmpColor.r
      lc[c++] = tmpColor.g
      lc[c++] = tmpColor.b
    }
  }
  for (let y = 0; y < rows; y++) {
    for (let x = 0; x < cols - 1; x++) emit(idx(x, y), idx(x + 1, y))
  }
  for (let y = 0; y < rows - 1; y++) {
    for (let x = 0; x < cols; x++) emit(idx(x, y), idx(x, y + 1))
  }
  while (a < MAX_SEGMENTS * 2) la[a++] = 0
  linePos.needsUpdate = true
  lineAlpha.needsUpdate = true
  lineColor.needsUpdate = true
}

const homeTmp = { x: 0, y: 0, z: 0 }

function layoutGrid() {
  if (!camera) return
  const viewW = camera.right - camera.left
  const viewH = camera.top - camera.bottom
  const pad = 1.16
  const needW = viewW * pad
  const needH = viewH * pad
  cols = Math.max(2, Math.min(MAX_COLS, Math.ceil(needW / CELL) + 1))
  rows = Math.max(2, Math.min(MAX_ROWS, Math.ceil(needH / CELL) + 1))
  cell = Math.max(needW / (cols - 1), needH / (rows - 1))
  brush = cell * 2.2
  count = cols * rows
  originX = -((cols - 1) * cell) / 2
  originY = -((rows - 1) * cell) / 2
  seedLattice()
  syncWaveSpace({ originX, originY, cell })
}

function resize() {
  const host = hostRef.value
  if (!host || !renderer || !camera) return
  const w = Math.max(2, host.clientWidth)
  const h = Math.max(2, host.clientHeight)
  renderer.setSize(w, h, false)
  PIXEL.value = renderer.getPixelRatio()
  const hh = 4.55
  const aspect = w / h
  camera.left = -hh * aspect
  camera.right = hh * aspect
  camera.top = hh
  camera.bottom = -hh
  camera.updateProjectionMatrix()
  spanX = hh * aspect * 2
  spanY = hh * 2
  syncWaveSpace({ halfW: camera.right, halfH: camera.top })
  if (wash) wash.scale.set(spanX * 1.16, spanY * 1.16, 1)
  layoutGrid()
  for (let i = 0; i < count; i++) {
    homeOf(i, 0, homeTmp)
    pos[i * 3] = homeTmp.x
    pos[i * 3 + 1] = homeTmp.y
    pos[i * 3 + 2] = homeTmp.z
  }
  if (posAttr) posAttr.needsUpdate = true
}

function step(now: number) {
  if (!running || !renderer || !scene || !camera || !posAttr || !sizeAttr || !colorAttr || !pointAlpha) return
  raf = requestAnimationFrame(step)
  const t = now * 0.001
  prunePulses(t)
  colorA.lerp(colorTargetA, 0.04)
  colorB.lerp(colorTargetB, 0.04)
  colorC.lerp(colorTargetC, 0.04)

  mouseVel.set(mouse.x - mousePrev.x, mouse.y - mousePrev.y)
  const moving = mouseVel.lengthSq() > 1e-8
  mousePrev.copy(mouse)

  const sizes = sizeAttr.array as Float32Array
  const colors = colorAttr.array as Float32Array
  const alphas = pointAlpha.array as Float32Array
  const mx = mouse.x * ((camera.right - camera.left) * 0.5)
  const my = mouse.y * ((camera.top - camera.bottom) * 0.5)

  for (let i = 0; i < count; i++) {
    const i3 = i * 3
    const w = homeOf(i, t, homeTmp)
    waveAmt[i] = w
    const dx = homeTmp.x - mx
    const dy = homeTmp.y - my
    const dist = Math.hypot(dx, dy)
    const intact = age[i] <= 0 && rebuild[i] > 0.92

    if (moving && intact && dist < brush) {
      age[i] = 0.001
      rebuild[i] = 0
      hold[i] = 0.28
      const ang = Math.atan2(homeTmp.y - my, homeTmp.x - mx)
      dirX[i] = Math.cos(ang)
      dirY[i] = Math.sin(ang)
      speed[i] = MOVE
      grain[i] = 3.4
      emitSparks(homeTmp.x, homeTmp.y, ang)
    }

    if (age[i] > 0 && age[i] < 1) {
      age[i] = Math.min(1, age[i] + AGE_STEP)
      const live = 1 - age[i]
      pos[i3] += dirX[i] * MOVE
      pos[i3 + 1] += dirY[i] * MOVE
      sizes[i] = grain[i]
      alphas[i] = live
      tmpColor.copy(white).lerp(colorA, 0.08)
    } else if (age[i] >= 1) {
      age[i] = 0
      pos[i3] = homeTmp.x
      pos[i3 + 1] = homeTmp.y
      pos[i3 + 2] = homeTmp.z
      sizes[i] = 0
      alphas[i] = 0
      tmpColor.copy(gridTint)
    } else {
      pos[i3] = homeTmp.x
      pos[i3 + 1] = homeTmp.y
      pos[i3 + 2] = homeTmp.z
      if (rebuild[i] < 1) {
        if (hold[i] > 0) hold[i] -= 0.016
        else rebuild[i] = Math.min(1, rebuild[i] + AGE_STEP * 0.55)
      }
      sizes[i] = (1.45 + w * 2.6) * rebuild[i]
      alphas[i] = (0.14 + w * 0.42) * rebuild[i]
      tmpColor.copy(gridTint).lerp(colorA, 0.08 + w * 0.38)
      tmpColor.lerp(white, w * 0.2)
    }
    colors[i3] = tmpColor.r
    colors[i3 + 1] = tmpColor.g
    colors[i3 + 2] = tmpColor.b
  }

  for (let i = count; i < MAX_COUNT; i++) {
    sizes[i] = 0
    alphas[i] = 0
  }

  for (let s = 0; s < MAX_SPARKS; s++) {
    const i = MAX_COUNT + s
    const i3 = i * 3
    if (age[i] > 0 && age[i] < 1) {
      age[i] = Math.min(1, age[i] + AGE_STEP)
      const live = 1 - age[i]
      pos[i3] += dirX[i] * MOVE
      pos[i3 + 1] += dirY[i] * MOVE
      sizes[i] = grain[i]
      alphas[i] = 0.92 * live
      tmpColor.copy(white).lerp(colorC, 0.18)
      colors[i3] = tmpColor.r
      colors[i3 + 1] = tmpColor.g
      colors[i3 + 2] = tmpColor.b
    } else {
      age[i] = 0
      sizes[i] = 0
      alphas[i] = 0
    }
  }

  posAttr.needsUpdate = true
  sizeAttr.needsUpdate = true
  colorAttr.needsUpdate = true
  pointAlpha.needsUpdate = true
  writeLines()

  if (washMat) {
    washMat.uniforms.uTime.value = t
    washMat.uniforms.uMouse.value.copy(mouse)
    washMat.uniforms.uA.value.copy(colorA)
    washMat.uniforms.uB.value.copy(colorB)
    washMat.uniforms.uC.value.copy(colorC)
  }

  renderer.render(scene, camera)
}

function onPointer(e: PointerEvent) {
  if (!renderer || !camera) return
  const rect = renderer.domElement.getBoundingClientRect()
  if (rect.width < 2 || rect.height < 2) return
  mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1
}

function onPointerDown(e: PointerEvent) {
  if (e.button !== 0 || !renderer || !camera) return
  const rect = renderer.domElement.getBoundingClientRect()
  if (rect.width < 2 || rect.height < 2) return
  const ndcX = ((e.clientX - rect.left) / rect.width) * 2 - 1
  const ndcY = -((e.clientY - rect.top) / rect.height) * 2 + 1
  mouse.x = ndcX
  mouse.y = ndcY
  spawnPulseFromNdc(ndcX, ndcY)
}

function onVisibility() {
  if (document.hidden) stop()
  else startLoop()
}

function startLoop() {
  if (running || !renderer) return
  running = true
  raf = requestAnimationFrame(step)
}

function stop() {
  running = false
  cancelAnimationFrame(raf)
}

function setup() {
  const host = hostRef.value
  if (!host) return
  applyPalette(props.sceneKey, true)
  scene = new THREE.Scene()
  camera = new THREE.OrthographicCamera(-1, 1, 1, -1, -8, 8)
  camera.position.z = 4
  renderer = new THREE.WebGLRenderer({
    alpha: true,
    antialias: true,
    powerPreference: 'high-performance',
  })
  THREE.ColorManagement.enabled = false
  renderer.setClearColor(0x000000, 0)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  PIXEL.value = renderer.getPixelRatio()
  renderer.outputColorSpace = THREE.LinearSRGBColorSpace
  renderer.toneMapping = THREE.NoToneMapping
  renderer.domElement.style.width = '100%'
  renderer.domElement.style.height = '100%'
  renderer.domElement.style.display = 'block'
  host.appendChild(renderer.domElement)

  washMat = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uMouse: { value: new THREE.Vector2(0, 0) },
      uA: { value: colorA.clone() },
      uB: { value: colorB.clone() },
      uC: { value: colorC.clone() },
    },
    vertexShader: washVert,
    fragmentShader: washFrag,
    transparent: true,
    depthWrite: false,
    toneMapped: false,
  })
  wash = new THREE.Mesh(new THREE.PlaneGeometry(1, 1), washMat)
  wash.position.z = -1.2
  scene.add(wash)

  const pointGeo = new THREE.BufferGeometry()
  pointGeo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
  pointGeo.setAttribute('aColor', new THREE.BufferAttribute(new Float32Array(POINT_TOTAL * 3), 3))
  pointGeo.setAttribute('aSize', new THREE.BufferAttribute(new Float32Array(POINT_TOTAL), 1))
  pointGeo.setAttribute('aAlpha', new THREE.BufferAttribute(new Float32Array(POINT_TOTAL), 1))
  posAttr = pointGeo.getAttribute('position') as THREE.BufferAttribute
  colorAttr = pointGeo.getAttribute('aColor') as THREE.BufferAttribute
  sizeAttr = pointGeo.getAttribute('aSize') as THREE.BufferAttribute
  pointAlpha = pointGeo.getAttribute('aAlpha') as THREE.BufferAttribute
  points = new THREE.Points(
    pointGeo,
    new THREE.ShaderMaterial({
      uniforms: { uPixelRatio: PIXEL },
      vertexShader: pointVert,
      fragmentShader: pointFrag,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
      toneMapped: false,
    }),
  )
  scene.add(points)

  const lineGeo = new THREE.BufferGeometry()
  lineGeo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(MAX_SEGMENTS * 2 * 3), 3))
  lineGeo.setAttribute('aColor', new THREE.BufferAttribute(new Float32Array(MAX_SEGMENTS * 2 * 3), 3))
  lineGeo.setAttribute('aAlpha', new THREE.BufferAttribute(new Float32Array(MAX_SEGMENTS * 2), 1))
  linePos = lineGeo.getAttribute('position') as THREE.BufferAttribute
  lineColor = lineGeo.getAttribute('aColor') as THREE.BufferAttribute
  lineAlpha = lineGeo.getAttribute('aAlpha') as THREE.BufferAttribute
  lines = new THREE.LineSegments(
    lineGeo,
    new THREE.ShaderMaterial({
      vertexShader: lineVert,
      fragmentShader: lineFrag,
      transparent: true,
      depthWrite: false,
      blending: THREE.AdditiveBlending,
      toneMapped: false,
    }),
  )
  scene.add(lines)

  resize()
  for (let i = 0; i < count; i++) {
    homeOf(i, 0, homeTmp)
    pos[i * 3] = homeTmp.x
    pos[i * 3 + 1] = homeTmp.y
    pos[i * 3 + 2] = homeTmp.z
  }
  writeLines()
  window.addEventListener('resize', resize)
  window.addEventListener('pointermove', onPointer, { passive: true })
  window.addEventListener('pointerdown', onPointerDown)
  document.addEventListener('visibilitychange', onVisibility)
  startLoop()
}

function teardown() {
  stop()
  window.removeEventListener('resize', resize)
  window.removeEventListener('pointermove', onPointer)
  window.removeEventListener('pointerdown', onPointerDown)
  document.removeEventListener('visibilitychange', onVisibility)
  clearPulses()
  wash?.geometry.dispose()
  washMat?.dispose()
  points?.geometry.dispose()
  ;(points?.material as THREE.Material | undefined)?.dispose()
  lines?.geometry.dispose()
  ;(lines?.material as THREE.Material | undefined)?.dispose()
  renderer?.dispose()
  renderer?.domElement.remove()
  renderer = null
  scene = null
  camera = null
  points = null
  lines = null
  wash = null
  washMat = null
  posAttr = null
  sizeAttr = null
  colorAttr = null
  pointAlpha = null
  linePos = null
  lineAlpha = null
  lineColor = null
}

onMounted(async () => {
  await nextTick()
  setup()
})

onUnmounted(teardown)

watch(
  () => props.sceneKey,
  (key) => applyPalette(key),
)
</script>

<template>
  <div ref="hostRef" class="login-three" aria-hidden="true" />
</template>

<style scoped>
.login-three {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
}
</style>
