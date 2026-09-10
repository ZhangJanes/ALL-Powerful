const MAX_PULSES = 10
export const WAVE_C = 15.5
export const WAVE_K = 0.55
export const WAVE_LIFE = 7.2
export const WAVE_LIFT = 2.65

type Pulse = { gx: number; gy: number; t0: number; amp: number }
const pulses: Pulse[] = []

export const waveSpace = {
  originX: 0,
  originY: 0,
  cell: 0.168,
  halfW: 4.55,
  halfH: 4.55,
}

export function syncWaveSpace(next: Partial<typeof waveSpace>) {
  Object.assign(waveSpace, next)
}

export function prunePulses(t: number) {
  for (let i = pulses.length - 1; i >= 0; i--) {
    if (t - pulses[i].t0 > WAVE_LIFE) pulses.splice(i, 1)
  }
}

export function clearPulses() {
  pulses.length = 0
}

export function spawnPulse(gx: number, gy: number, t = performance.now() * 0.001) {
  pulses.push({ gx, gy, t0: t, amp: 1 })
  if (pulses.length > MAX_PULSES) pulses.shift()
}

export function spawnPulseFromNdc(ndcX: number, ndcY: number) {
  const wx = ndcX * waveSpace.halfW
  const wy = ndcY * waveSpace.halfH
  spawnPulse((wx - waveSpace.originX) / waveSpace.cell, (wy - waveSpace.originY) / waveSpace.cell)
}

export function samplePulses(gx: number, gy: number, t: number) {
  let ox = 0
  let oy = 0
  let crest = 0
  let height = 0
  const lambda = (Math.PI * 2) / WAVE_K
  const span = lambda * 2.35
  const cell = waveSpace.cell
  for (let i = 0; i < pulses.length; i++) {
    const p = pulses[i]
    const elapsed = t - p.t0
    if (elapsed < 0 || elapsed > WAVE_LIFE) continue
    const dx = gx - p.gx
    const dy = gy - p.gy
    const r = Math.hypot(dx, dy)
    const life = Math.min(1, elapsed / WAVE_LIFE)
    const front = WAVE_C * elapsed * (1 - 0.09 * life)
    const behind = front - r
    if (behind < -2.4 || behind > span + lambda * 0.35) continue
    const lead = 1 / (1 + Math.exp(-behind * 1.35))
    const u = Math.min(1, Math.max(0, behind / span))
    const taper = 0.5 + 0.5 * Math.cos(Math.PI * u)
    const born = 1 - Math.exp(-elapsed * 2.6)
    const env = lead * taper * born
    const fade = (1 - 0.72 * life) / (1 + r * 0.028)
    const theta = WAVE_K * behind
    const s = Math.sin(theta) * 0.78 + Math.sin(theta * 0.5) * 0.22
    const mag = p.amp * s * env * fade
    const inv = 1 / (r + 0.08)
    ox += dx * inv * mag * cell * WAVE_LIFT
    oy += dy * inv * mag * cell * WAVE_LIFT
    height += s * env * fade
    const hit = Math.max(0, s) * env * fade
    if (hit > crest) crest = hit
  }
  return { x: ox, y: oy, crest: Math.min(1, crest), height }
}

export function worldToPixels(wx: number, wy: number) {
  return {
    x: (wx / waveSpace.halfW) * (window.innerWidth * 0.5),
    y: (-wy / waveSpace.halfH) * (window.innerHeight * 0.5),
  }
}

export function fillDisplacementMap(
  imageData: ImageData,
  rest: { cx: number; cy: number; w: number; h: number },
  t: number,
  scale: number,
) {
  const { data, width, height } = imageData
  let i = 0
  for (let y = 0; y < height; y++) {
    const cy = rest.cy - rest.h * 0.5 + ((y + 0.5) / height) * rest.h
    for (let x = 0; x < width; x++) {
      const cx = rest.cx - rest.w * 0.5 + ((x + 0.5) / width) * rest.w
      const s = sampleAtClient(cx, cy, t)
      const p = worldToPixels(s.x, s.y)
      const dx = p.x * 0.55
      const dy = -s.height * 58 + p.y * 0.35
      data[i] = 128 + Math.max(-127, Math.min(127, (dx / scale) * 127))
      data[i + 1] = 128 + Math.max(-127, Math.min(127, (dy / scale) * 127))
      data[i + 2] = 128
      data[i + 3] = 255
      i += 4
    }
  }
}

export function clientToGrid(clientX: number, clientY: number) {
  const ndcX = (clientX / Math.max(window.innerWidth, 1)) * 2 - 1
  const ndcY = -((clientY / Math.max(window.innerHeight, 1)) * 2) + 1
  const wx = ndcX * waveSpace.halfW
  const wy = ndcY * waveSpace.halfH
  return {
    gx: (wx - waveSpace.originX) / waveSpace.cell,
    gy: (wy - waveSpace.originY) / waveSpace.cell,
  }
}

export function sampleAtClient(clientX: number, clientY: number, t: number) {
  const g = clientToGrid(clientX, clientY)
  return samplePulses(g.gx, g.gy, t)
}
