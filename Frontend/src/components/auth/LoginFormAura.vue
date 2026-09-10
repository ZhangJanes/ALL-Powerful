<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as THREE from 'three'

const props = defineProps<{
  sceneKey: string
}>()

const hostRef = ref<HTMLDivElement | null>(null)

const PALETTES: Record<string, [string, string, string]> = {
  journal: ['#8ab4f8', '#d2e3fc', '#fde293'],
  health: ['#81c995', '#ceead6', '#fde293'],
  rhythm: ['#fbbc05', '#fde293', '#f6aea9'],
  voyage: ['#f9a8d4', '#fdba74', '#c4b5fd'],
  vault: ['#a5b4fc', '#c7d2fe', '#67e8f9'],
}

let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.OrthographicCamera | null = null
let mesh: THREE.Mesh | null = null
let mat: THREE.ShaderMaterial | null = null
let raf = 0
let running = false
let ro: ResizeObserver | null = null

const mouse = new THREE.Vector2(0.18, 0.72)
const mouseSmooth = new THREE.Vector2(0.18, 0.72)
const colorA = new THREE.Color()
const colorB = new THREE.Color()
const colorC = new THREE.Color()
const colorTargetA = new THREE.Color()
const colorTargetB = new THREE.Color()
const colorTargetC = new THREE.Color()

const vert = `
varying vec2 vUv;
void main() {
  vUv = uv;
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
`
const frag = `
precision highp float;
uniform float uTime;
uniform vec2 uMouse;
uniform vec2 uRes;
uniform vec3 uA;
uniform vec3 uB;
uniform vec3 uC;
varying vec2 vUv;

void main() {
  vec2 uv = vUv;
  float t = uTime;
  float aspect = max(uRes.x / max(uRes.y, 1.0), 1.0);
  float edge = 1.0 - min(min(uv.x, 1.0 - uv.x) * aspect, min(uv.y, 1.0 - uv.y)) * 8.4;
  edge = clamp(pow(max(edge, 0.0), 1.35), 0.0, 1.0);

  float sweep = uv.x * 0.72 + uv.y * 0.38 - 0.18 - sin(t * 0.22) * 0.28;
  float spec = pow(max(0.0, 1.0 - abs(sweep) * 6.4), 10.0);
  float caustic = 0.5 + 0.5 * sin((uv.x * 7.2 + uv.y * 3.1) + t * 0.35);
  caustic *= 0.5 + 0.5 * sin((uv.y * 9.0 - uv.x * 2.4) - t * 0.18);

  vec2 m = uMouse;
  float md = length((uv - m) * vec2(aspect, 1.0));
  float glow = exp(-md * 4.8);

  vec3 iris = mix(uA, uB, 0.45 + 0.45 * sin(t * 0.16 + uv.x * 3.2));
  iris = mix(iris, uC, 0.18 + 0.18 * caustic);
  vec3 col = iris * (0.16 + edge * 0.55);
  col += uB * spec * 0.55;
  col += uC * glow * 0.28;
  col += vec3(1.0) * spec * 0.22;

  float alpha = 0.08 + edge * 0.34 + spec * 0.16 + glow * 0.08;
  gl_FragColor = vec4(col, clamp(alpha, 0.0, 0.52));
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

function resize() {
  const host = hostRef.value
  if (!host || !renderer || !camera || !mat) return
  const w = Math.max(2, host.clientWidth)
  const h = Math.max(2, host.clientHeight)
  renderer.setSize(w, h, false)
  const hh = 1
  const aspect = w / h
  camera.left = -hh * aspect
  camera.right = hh * aspect
  camera.top = hh
  camera.bottom = -hh
  camera.updateProjectionMatrix()
  if (mesh) mesh.scale.set(hh * aspect * 2, hh * 2, 1)
  mat.uniforms.uRes.value.set(w, h)
}

function step(now: number) {
  if (!running || !renderer || !scene || !camera || !mat) return
  raf = requestAnimationFrame(step)
  mouseSmooth.lerp(mouse, 0.06)
  colorA.lerp(colorTargetA, 0.045)
  colorB.lerp(colorTargetB, 0.045)
  colorC.lerp(colorTargetC, 0.045)
  mat.uniforms.uTime.value = now * 0.001
  mat.uniforms.uMouse.value.copy(mouseSmooth)
  mat.uniforms.uA.value.copy(colorA)
  mat.uniforms.uB.value.copy(colorB)
  mat.uniforms.uC.value.copy(colorC)
  renderer.render(scene, camera)
}

function onPointer(e: PointerEvent) {
  const host = hostRef.value
  if (!host) return
  const rect = host.getBoundingClientRect()
  if (rect.width < 2 || rect.height < 2) return
  mouse.x = (e.clientX - rect.left) / rect.width
  mouse.y = 1 - (e.clientY - rect.top) / rect.height
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
  camera = new THREE.OrthographicCamera(-1, 1, 1, -1, -4, 4)
  camera.position.z = 2
  renderer = new THREE.WebGLRenderer({
    alpha: true,
    antialias: true,
    powerPreference: 'high-performance',
  })
  THREE.ColorManagement.enabled = false
  renderer.setClearColor(0x000000, 0)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.outputColorSpace = THREE.LinearSRGBColorSpace
  renderer.toneMapping = THREE.NoToneMapping
  renderer.domElement.style.width = '100%'
  renderer.domElement.style.height = '100%'
  renderer.domElement.style.display = 'block'
  host.appendChild(renderer.domElement)

  mat = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uMouse: { value: mouseSmooth.clone() },
      uRes: { value: new THREE.Vector2(1, 1) },
      uA: { value: colorA.clone() },
      uB: { value: colorB.clone() },
      uC: { value: colorC.clone() },
    },
    vertexShader: vert,
    fragmentShader: frag,
    transparent: true,
    depthWrite: false,
    toneMapped: false,
  })
  mesh = new THREE.Mesh(new THREE.PlaneGeometry(1, 1), mat)
  scene.add(mesh)

  resize()
  ro = new ResizeObserver(resize)
  ro.observe(host)
  window.addEventListener('pointermove', onPointer, { passive: true })
  document.addEventListener('visibilitychange', onVisibility)
  startLoop()
}

function teardown() {
  stop()
  window.removeEventListener('pointermove', onPointer)
  document.removeEventListener('visibilitychange', onVisibility)
  ro?.disconnect()
  ro = null
  mesh?.geometry.dispose()
  mat?.dispose()
  renderer?.dispose()
  renderer?.domElement.remove()
  renderer = null
  scene = null
  camera = null
  mesh = null
  mat = null
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
  <div ref="hostRef" class="form-aura" aria-hidden="true" />
</template>

<style scoped>
.form-aura {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  border-radius: 24px;
  pointer-events: none;
}
</style>
