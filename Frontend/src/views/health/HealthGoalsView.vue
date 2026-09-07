<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { AddOutline, FlagOutline } from '@vicons/ionicons5'
import { useDialog, useMessage } from 'naive-ui'
import HealthModuleNav from '@/components/health/HealthModuleNav.vue'
import { useHealthStore } from '@/stores/health'
import type { HealthGoalDto, HealthGoalPayload } from '@/api/health'

const store = useHealthStore()
const dialog = useDialog()
const message = useMessage()
const showEditor = ref(false)
const editingId = ref<number>()
function todayYmd() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}
const form = reactive({
  goalType: 'WEIGHT',
  title: '',
  targetValue: 60,
  unit: 'kg',
  periodType: 'once' as HealthGoalPayload['periodType'],
  comparisonOperator: 'LTE' as HealthGoalPayload['comparisonOperator'],
  startDate: todayYmd(),
  deadline: null as string | null,
  status: 'active' as HealthGoalPayload['status'],
})

const typeOptions = [
  { label: '体重', value: 'WEIGHT', unit: 'kg', operator: 'LTE', period: 'once' },
  { label: '体脂率', value: 'BODY_FAT', unit: '%', operator: 'LTE', period: 'once' },
  { label: '目标热量', value: 'CALORIES', unit: 'kcal', operator: 'LTE', period: 'daily' },
  { label: '饮水', value: 'WATER', unit: 'ml', operator: 'GTE', period: 'daily' },
  { label: '睡眠', value: 'SLEEP', unit: '分钟', operator: 'GTE', period: 'daily' },
  { label: '有氧运动', value: 'AEROBIC', unit: '次', operator: 'GTE', period: 'weekly' },
  { label: '阻抗运动', value: 'RESISTANCE', unit: '次', operator: 'GTE', period: 'weekly' },
  { label: '餐次执行', value: 'MEAL_ADHERENCE', unit: '餐', operator: 'GTE', period: 'daily' },
]

function openCreate() {
  editingId.value = undefined
  Object.assign(form, {
    goalType: 'WEIGHT',
    title: '目标体重',
    targetValue: 60,
    unit: 'kg',
    periodType: 'once',
    comparisonOperator: 'LTE',
    startDate: todayYmd(),
    deadline: null,
    status: 'active',
  })
  showEditor.value = true
}

function openEdit(goal: HealthGoalDto) {
  editingId.value = goal.id
  Object.assign(form, { ...goal, deadline: goal.deadline || null })
  showEditor.value = true
}

function onTypeChange(value: string) {
  const option = typeOptions.find((x) => x.value === value)
  if (!option) return
  form.title = option.label
  form.unit = option.unit
  form.comparisonOperator = option.operator as HealthGoalPayload['comparisonOperator']
  form.periodType = option.period as HealthGoalPayload['periodType']
}

async function save() {
  if (!form.title.trim() || !form.targetValue || form.targetValue <= 0) {
    message.warning('请填写目标名称和有效数值')
    return
  }
  await store.saveGoal({
    goalType: form.goalType,
    title: form.title,
    targetValue: form.targetValue,
    unit: form.unit,
    periodType: form.periodType,
    comparisonOperator: form.comparisonOperator,
    startDate: form.startDate,
    deadline: form.deadline || undefined,
    status: form.status,
  }, editingId.value)
  showEditor.value = false
  message.success(editingId.value ? '目标已更新' : '目标已建立')
}

function operatorSymbol(operator: string) {
  return ({ GT: '>', GTE: '≥', LT: '<', LTE: '≤', EQ: '=' } as Record<string, string>)[operator] || operator
}

function remove(goal: HealthGoalDto) {
  dialog.warning({
    title: '删除目标',
    content: `确认删除「${goal.title}」？历史健康记录不会被删除。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: () => store.removeGoal(goal.id),
  })
}

onMounted(() => void store.syncGoals())
</script>

<template>
  <div class="app-shell page health-page">
    <HealthModuleNav title="目标计划" eyebrow="GOALS / MEASURABLE" />
    <NAlert v-if="store.error" type="error">{{ store.error }}</NAlert>

    <section class="goals-hero glass">
      <div>
        <span>FOCUS</span>
        <h2>目标要能记录，<br>也要能被解释。</h2>
        <p>图片中的饮水、餐次和运动目标已预置；体重、体脂与热量目标由张佳宁本人填写。</p>
      </div>
      <NButton type="primary" size="large" round @click="openCreate">
        新建目标
        <template #icon><NIcon :component="AddOutline" /></template>
      </NButton>
    </section>

    <div v-if="store.goals.length" class="goals-grid">
      <article v-for="goal in store.goals" :key="goal.id" class="goal-card glass">
        <div class="goal-top">
          <div class="goal-icon"><NIcon :component="FlagOutline" :size="20" /></div>
          <NTag :type="goal.status === 'active' ? 'success' : 'default'" round>
            {{ goal.status === 'active' ? '进行中' : goal.status === 'paused' ? '已暂停' : '已完成' }}
          </NTag>
        </div>
        <span class="goal-type">{{ goal.goalType }}</span>
        <h3>{{ goal.title }}</h3>
        <div class="target">
          <strong>{{ operatorSymbol(goal.comparisonOperator) }}{{ goal.targetValue }}</strong>
          <span>{{ goal.unit }} / {{ goal.periodType === 'daily' ? '每日' : goal.periodType === 'weekly' ? '每周' : goal.periodType === 'monthly' ? '每月' : '目标期' }}</span>
        </div>
        <div class="dates">{{ goal.startDate }}<template v-if="goal.deadline"> → {{ goal.deadline }}</template></div>
        <div class="goal-actions">
          <NButton size="small" @click="openEdit(goal)">编辑</NButton>
          <NButton size="small" type="error" ghost @click="remove(goal)">删除</NButton>
        </div>
      </article>
    </div>
    <NEmpty v-else-if="!store.loading" description="还没有目标计划">
      <template #extra><NButton type="primary" @click="openCreate">建立第一个目标</NButton></template>
    </NEmpty>

    <NModal v-model:show="showEditor" preset="card" :title="editingId ? '编辑目标' : '新建目标'" class="goal-modal">
      <NForm label-placement="top">
        <NGrid :cols="2" :x-gap="14" :y-gap="14">
          <NFormItemGridItem label="目标类型">
            <NSelect v-model:value="form.goalType" :options="typeOptions" @update:value="onTypeChange" />
          </NFormItemGridItem>
          <NFormItemGridItem label="目标名称"><NInput v-model:value="form.title" /></NFormItemGridItem>
          <NFormItemGridItem label="目标数值"><NInputNumber v-model:value="form.targetValue" :min="0.01" /></NFormItemGridItem>
          <NFormItemGridItem label="单位"><NInput v-model:value="form.unit" /></NFormItemGridItem>
          <NFormItemGridItem label="周期">
            <NSelect v-model:value="form.periodType" :options="[
              { label: '每日', value: 'daily' }, { label: '每周', value: 'weekly' },
              { label: '每月', value: 'monthly' }, { label: '目标期', value: 'once' },
            ]" />
          </NFormItemGridItem>
          <NFormItemGridItem label="达标条件">
            <NSelect v-model:value="form.comparisonOperator" :options="[
              { label: '大于等于', value: 'GTE' }, { label: '大于', value: 'GT' },
              { label: '小于等于', value: 'LTE' }, { label: '小于', value: 'LT' },
              { label: '等于', value: 'EQ' },
            ]" />
          </NFormItemGridItem>
          <NFormItemGridItem label="开始日期"><NDatePicker v-model:formatted-value="form.startDate" type="date" value-format="yyyy-MM-dd" /></NFormItemGridItem>
          <NFormItemGridItem label="截止日期"><NDatePicker v-model:formatted-value="form.deadline" type="date" value-format="yyyy-MM-dd" clearable /></NFormItemGridItem>
        </NGrid>
        <div class="modal-actions"><NButton @click="showEditor = false">取消</NButton><NButton type="primary" @click="save">保存目标</NButton></div>
      </NForm>
    </NModal>
  </div>
</template>

<style scoped>
.goals-hero { display: flex; align-items: end; justify-content: space-between; gap: 20px; padding: 24px; border: 1px solid rgba(45,212,191,.15); border-radius: var(--fm-radius-xl); background: radial-gradient(circle at 85% 15%,rgba(139,92,246,.16),transparent 30%); }
.goals-hero > div > span,.goal-type { color: #2dd4bf; font-size: 10px; font-weight: 800; letter-spacing: .16em; }
.goals-hero h2 { margin: 10px 0; color: var(--fm-text-strong); font-size: clamp(30px,4vw,48px); line-height: 1.05; letter-spacing: -.04em; }
.goals-hero p { margin: 0; color: var(--fm-text-muted); }
.goals-grid { display: grid; grid-template-columns: repeat(auto-fit,minmax(260px,1fr)); gap: 20px; }
.goal-card { padding: 17px; border: 1px solid var(--fm-track); border-radius: var(--fm-radius-lg); }
.goal-top,.goal-actions { display: flex; justify-content: space-between; align-items: center; }
.goal-icon { display: grid; width: 42px; height: 42px; place-content: center; border-radius: 13px; color: #99f6e4; background: rgba(13,148,136,.18); }
.goal-type { display: block; margin-top: 22px; }
.goal-card h3 { margin: 7px 0 18px; color: var(--fm-text-strong); font-size: 20px; }
.target { display: flex; align-items: baseline; gap: 8px; }
.target strong { color: var(--fm-text-strong); font-size: 34px; letter-spacing: -.04em; }
.target span,.dates { color: var(--fm-text-muted); font-size: 12px; }
.dates { margin: 12px 0 18px; }
.goal-modal { width: min(620px,calc(100vw - 24px)); }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
@media (max-width: 700px) { .goals-hero { align-items: stretch; flex-direction: column; } }
</style>
