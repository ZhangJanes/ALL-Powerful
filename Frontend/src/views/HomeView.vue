<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
    DocumentTextOutline,
    WalletOutline,
    CheckboxOutline,
    NutritionOutline,
    AirplaneOutline,
    BulbOutline,
    ImagesOutline,
    ReaderOutline,
    GridOutline,
    PartlySunnyOutline,
} from '@vicons/ionicons5'
import { useMemoStore } from '@/stores/memo'
import { useLedgerStore } from '@/stores/ledger'
import { useHabitStore } from '@/stores/habit'
import { useHealthStore } from '@/stores/health'
import { useMessageStore } from '@/stores/message'
import { useWeatherStore } from '@/stores/weather'
import { useSettingsStore } from '@/stores/settings'
import FmChartBlock from '@/components/charts/FmChartBlock.vue'
import {
    aggregateExpenseByDay,
    buildBudgetRingOption,
    buildExpenseLineBarOption,
} from '@/utils/chartOptions'

const router = useRouter()
const memoStore = useMemoStore()
const ledgerStore = useLedgerStore()
const habitStore = useHabitStore()
const healthStore = useHealthStore()
const messageStore = useMessageStore()
const weatherStore = useWeatherStore()
const settingsStore = useSettingsStore()

const weatherModal = ref(false)

const homeLineMixOption = computed(() => {
    const { categories, values } = aggregateExpenseByDay(ledgerStore.ledger, 7)
    return buildExpenseLineBarOption(categories, values)
})

const homeBudgetRing = computed(() =>
    buildBudgetRingOption(ledgerStore.monthlySpent, ledgerStore.monthlyBudget),
)

const shortcuts = [
    {
        label: '备忘录',
        name: 'memo',
        icon: DocumentTextOutline,
        color: '#14b8a6',
    },
    { label: '记账', name: 'ledger', icon: WalletOutline, color: '#f472b6' },
    { label: '打卡', name: 'habits', icon: CheckboxOutline, color: '#34d399' },
    { label: '健康饮食', name: 'health', icon: NutritionOutline, color: '#2dd4bf' },
    { label: '出行', name: 'travel', icon: AirplaneOutline, color: '#f97316' },
    { label: 'New Idea', name: 'ideas', icon: BulbOutline, color: '#fbbf24' },
    { label: '照片', name: 'photos', icon: ImagesOutline, color: '#0d9488' },
    { label: '办事指南', name: 'guide', icon: ReaderOutline, color: '#94a3b8' },
    { label: '更多', name: 'more', icon: GridOutline, color: '#cbd5e1' },
] as const

const weekday = new Intl.DateTimeFormat('zh-CN', { weekday: 'long' }).format(
    new Date(),
)
const dateStr = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
}).format(new Date())

function openWeather() {
    weatherModal.value = true
}

async function refreshWeather() {
    try {
        await weatherStore.syncWeather()
    } catch {
        // store 已记录 error，界面展示即可
    }
}

function goProfile() {
    weatherModal.value = false
    void router.push({ name: 'profile' })
}

onMounted(() => {
    void weatherStore.syncWeather().catch(() => undefined)
    const now = new Date()
    const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
    void healthStore.syncDashboard(today).catch(() => undefined)
})

watch(
    () => settingsStore.weatherCity,
    () => {
        void weatherStore.syncWeather().catch(() => undefined)
    },
)
</script>

<template>
    <div class="app-shell page home-page">
        <div class="hero glass">
            <div class="hero-top">
                <div>
                    <div class="date-line">{{ dateStr }} {{ weekday }}</div>
                    <button class="weather" type="button" @click="openWeather">
                        <NIcon :component="PartlySunnyOutline" :size="16" />
                        <span>{{ weatherStore.summary }}</span>
                    </button>
                </div>
                <NAvatar
                    round
                    :size="44"
                    class="avatar"
                    @click="router.push({ name: 'profile' })"
                    >家</NAvatar
                >
            </div>
            <div class="todo-hint" @click="router.push({ name: 'memo' })">
                今日待办：<NGradientText type="success"
                    >{{ memoStore.todoProgress.done }}/{{
                        memoStore.todoProgress.total
                    }}</NGradientText
                >
                件
            </div>
        </div>

        <NCard
            class="glass card-block"
            :bordered="false"
            size="small"
            title="未来 5 天天气"
        >
            <div v-if="weatherStore.loading && !weatherStore.weather" class="weather-hint">
                正在获取天气…
            </div>
            <div v-else-if="weatherStore.error && !weatherStore.weather" class="weather-hint danger">
                {{ weatherStore.error }}
                <NButton text type="primary" @click="refreshWeather">重试</NButton>
            </div>
            <div v-else class="forecast-row">
                <button
                    v-for="day in weatherStore.weather?.daily || []"
                    :key="day.date"
                    type="button"
                    class="forecast-day"
                    @click="openWeather"
                >
                    <div class="forecast-week">{{ day.weekday }}</div>
                    <div class="forecast-desc">{{ day.description }}</div>
                    <div class="forecast-temp">
                        {{ Math.round(day.tempMax) }}° / {{ Math.round(day.tempMin) }}°
                    </div>
                    <div v-if="day.precipProbability != null" class="forecast-rain">
                        降水 {{ day.precipProbability }}%
                    </div>
                </button>
            </div>
        </NCard>

        <NCard
            class="glass card-block"
            :bordered="false"
            size="small"
            title="今日速览"
        >
            <NGrid cols="1 620:3 1000:5" responsive="self" :x-gap="16" :y-gap="12">
                <NGridItem>
                    <div class="mini" @click="router.push({ name: 'memo' })">
                        <NStatistic
                            label="今日待办"
                            :value="`${memoStore.todoProgress.done}/${memoStore.todoProgress.total}`"
                            tabular-nums
                        />
                    </div>
                </NGridItem>
                <NGridItem>
                    <div class="mini" @click="router.push({ name: 'ledger' })">
                        <NStatistic
                            label="今日支出"
                            :value="`¥${ledgerStore.todayExpense}`"
                            tabular-nums
                        />
                    </div>
                </NGridItem>
                <NGridItem>
                    <div
                        class="mini"
                        @click="router.push({ name: 'ledger-budget' })"
                    >
                        <NStatistic
                            label="预算剩余"
                            :value="`¥${ledgerStore.budgetLeft}`"
                            tabular-nums
                            class="stat-tight"
                        />
                        <div class="sub">
                            / ¥{{ ledgerStore.monthlyBudget }}
                        </div>
                    </div>
                </NGridItem>
                <NGridItem>
                    <div class="mini" @click="router.push({ name: 'habits' })">
                        <NStatistic
                            label="今日打卡"
                            :value="`${habitStore.habitToday.done}/${habitStore.habitToday.total}`"
                            tabular-nums
                        />
                    </div>
                </NGridItem>
                <NGridItem>
                    <div class="mini" @click="router.push({ name: 'health' })">
                        <NStatistic
                            label="饮食进度"
                            :value="`${healthStore.dashboard?.overallProgressPercent || 0}%`"
                            tabular-nums
                        />
                    </div>
                </NGridItem>
            </NGrid>
            <div class="trip-line" @click="router.push({ name: 'travel' })">
                <NTag size="small" type="info" round>出行</NTag>
                <span>3 天后 · 周末出游（示例）</span>
            </div>
        </NCard>

        <NCard
            class="glass card-block"
            :bordered="false"
            size="small"
            title="支出与预算"
        >
            <NGrid :cols="2" :x-gap="20" :y-gap="8">
                <NGridItem>
                    <div class="chart-cap">近 7 日支出走势</div>
                    <FmChartBlock :option="homeLineMixOption" :height="340" />
                </NGridItem>
                <NGridItem>
                    <div class="chart-cap">本月预算使用</div>
                    <FmChartBlock :option="homeBudgetRing" :height="340" />
                </NGridItem>
            </NGrid>
        </NCard>

        <NCard
            class="glass card-block"
            :bordered="false"
            size="small"
            title="快捷入口"
        >
            <NGrid cols="2 680:5 1100:9" responsive="self" :x-gap="16" :y-gap="16">
                <NGridItem v-for="s in shortcuts" :key="s.name">
                    <div
                        class="shortcut"
                        @click="router.push({ name: s.name })"
                    >
                        <div
                            class="shortcut-icon"
                            :style="{
                                background: `${s.color}22`,
                                color: s.color,
                            }"
                        >
                            <NIcon :component="s.icon" :size="22" />
                        </div>
                        <div class="shortcut-label">{{ s.label }}</div>
                    </div>
                </NGridItem>
            </NGrid>
        </NCard>

        <NCard
            class="glass card-block"
            :bordered="false"
            size="small"
            title="最近动态"
        >
            <NList v-if="messageStore.activities.length" clickable hoverable>
                <NListItem
                    v-for="a in messageStore.activities.slice(0, 10)"
                    :key="a.id"
                >
                    <div class="act">
                        <div class="act-text">{{ a.text }}</div>
                        <div class="act-time">{{ a.at }}</div>
                    </div>
                </NListItem>
            </NList>
            <FmEmptyIllustrated
                v-else
                description="暂无动态，开始记录你的生活吧～"
                variant="empty"
            />
        </NCard>

        <NModal
            v-model:show="weatherModal"
            preset="card"
            style="width: min(520px, calc(100vw - 24px))"
            :title="`天气 · ${weatherStore.weather?.city || weatherStore.city}`"
        >
            <div class="weather-modal">
                <div class="city-row">
                    <div class="city-tip">
                        城市：{{ weatherStore.city || settingsStore.weatherCity }}
                        <button class="linkish" type="button" @click="goProfile">去个人页修改</button>
                    </div>
                    <NButton type="primary" :loading="weatherStore.loading" @click="refreshWeather">
                        刷新
                    </NButton>
                </div>

                <div v-if="weatherStore.weather" class="current-block">
                    <div class="current-main">
                        {{ weatherStore.weather.current.description }}
                        {{ Math.round(weatherStore.weather.current.temperature) }}℃
                    </div>
                    <div class="current-meta">
                        <span v-if="weatherStore.weather.current.humidity != null">
                            湿度 {{ weatherStore.weather.current.humidity }}%
                        </span>
                        <span v-if="weatherStore.weather.current.windSpeed != null">
                            风速 {{ weatherStore.weather.current.windSpeed }} km/h
                        </span>
                    </div>
                </div>
                <div v-else-if="weatherStore.error" class="weather-hint danger">
                    {{ weatherStore.error }}
                </div>

                <div class="modal-forecast">
                    <div
                        v-for="day in weatherStore.weather?.daily || []"
                        :key="day.date"
                        class="modal-day"
                    >
                        <div>
                            <div class="forecast-week">{{ day.weekday }}</div>
                            <div class="forecast-date">{{ day.date.slice(5) }}</div>
                        </div>
                        <div class="forecast-desc">{{ day.description }}</div>
                        <div class="forecast-temp">
                            {{ Math.round(day.tempMax) }}° / {{ Math.round(day.tempMin) }}°
                        </div>
                        <div class="forecast-rain">
                            {{ day.precipProbability != null ? `降水 ${day.precipProbability}%` : '—' }}
                        </div>
                    </div>
                </div>
            </div>
        </NModal>
    </div>
</template>

<style scoped>
.home-page {
    position: relative;
}

.hero {
    margin: 0;
    padding: 16px 19px 14px;
    border-radius: var(--fm-radius-lg);
    border: 1px solid var(--fm-track);
}
.hero-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
}
.date-line {
    font-size: var(--fm-font-title);
    line-height: 28px;
    font-weight: 700;
    letter-spacing: -0.018em;
}
.weather {
    margin-top: 6px;
    color: var(--fm-text-muted);
    font-size: 14px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 0;
    border: 0;
    background: transparent;
    cursor: pointer;
}
.weather:hover {
    color: var(--fm-text-secondary);
}
.weather-hint {
    font-size: 13px;
    color: var(--fm-text-muted);
    display: flex;
    align-items: center;
    gap: 8px;
}
.weather-hint.danger {
    color: #f87171;
}
.forecast-row {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 8px;
}
.forecast-day {
    border: 1px solid var(--fm-track);
    border-radius: var(--fm-radius-md);
    background: rgba(148, 163, 184, 0.06);
    padding: 9px 8px;
    text-align: center;
    cursor: pointer;
    color: inherit;
    transition: transform 160ms ease, box-shadow 160ms ease, background-color 160ms ease;
}
.forecast-day:hover {
    transform: translateY(-1px);
    background: rgba(148, 163, 184, 0.09);
    box-shadow: var(--fm-shadow-sm);
}
.forecast-week {
    font-size: 13px;
    font-weight: 700;
}
.forecast-desc {
    margin-top: 6px;
    font-size: 13px;
    color: var(--fm-text-secondary);
}
.forecast-temp {
    margin-top: 6px;
    font-size: 12px;
    color: var(--fm-text-muted);
}
.forecast-rain {
    margin-top: 4px;
    font-size: 11px;
    color: var(--fm-text-faint);
}
.weather-modal {
    display: flex;
    flex-direction: column;
    gap: 16px;
}
.city-row {
    display: flex;
    gap: 8px;
    align-items: center;
    justify-content: space-between;
}
.city-tip {
    font-size: 13px;
    color: var(--fm-text-secondary);
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
}
.linkish {
    border: 0;
    background: transparent;
    padding: 0;
    color: var(--fm-primary, #14b8a6);
    cursor: pointer;
    font-size: 13px;
}
.linkish:hover {
    text-decoration: underline;
}
.current-block {
    padding: 12px 14px;
    border-radius: 12px;
    background: rgba(20, 184, 166, 0.1);
}
.current-main {
    font-size: 22px;
    font-weight: 700;
}
.current-meta {
    margin-top: 6px;
    display: flex;
    gap: 14px;
    font-size: 13px;
    color: var(--fm-text-muted);
}
.modal-forecast {
    display: flex;
    flex-direction: column;
    gap: 8px;
}
.modal-day {
    display: grid;
    grid-template-columns: 72px 1fr 88px 76px;
    gap: 8px;
    align-items: center;
    padding: 10px 8px;
    border-radius: 10px;
    background: rgba(148, 163, 184, 0.08);
}
.forecast-date {
    font-size: 11px;
    color: var(--fm-text-faint);
}

@media (max-width: 720px) {
    .forecast-row {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    .modal-day {
        grid-template-columns: 64px 1fr;
        grid-template-rows: auto auto;
    }
}
.avatar {
    cursor: pointer;
    background: linear-gradient(135deg, #14b8a6, #64748b);
}
.todo-hint {
    margin-top: 10px;
    font-size: 14px;
    cursor: pointer;
    color: #fdba74;
}
.card-block {
    margin: 0;
    border-radius: var(--fm-radius-lg);
    border: 1px solid var(--fm-track);
}
.mini {
    cursor: pointer;
    padding: 4px 0;
}
.stat-tight :deep(.n-statistic-value) {
    font-size: 15px;
}
.sub {
    font-size: 11px;
    color: var(--fm-text-faint);
    margin-top: -4px;
}
.trip-line {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--fm-text-secondary);
    cursor: pointer;
}
.shortcut {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    cursor: pointer;
}
.shortcut-icon {
    width: 42px;
    height: 42px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid var(--fm-track);
}
.shortcut-label {
    font-size: 12px;
    color: var(--fm-text-secondary);
    text-align: center;
}
.act {
    width: 100%;
    display: flex;
    justify-content: space-between;
    gap: 10px;
}
.act-text {
    font-size: 14px;
}
.act-time {
    font-size: 12px;
    color: var(--fm-text-faint);
    white-space: nowrap;
}
.chart-cap {
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.02em;
    color: var(--fm-text-muted);
    margin-bottom: 8px;
}

:global(html[data-theme='light']) .date-line {
    color: var(--fm-text-strong);
}
:global(html[data-theme='light']) .trip-line {
    color: #0284c7;
}
:global(html[data-theme='light']) .todo-hint {
    color: #c2410c;
}
</style>
