<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline, AnalyticsOutline } from '@vicons/ionicons5'
import { useLedgerStore } from '@/stores/ledger'
import { useMessage } from 'naive-ui'

const router = useRouter()
const ledgerStore = useLedgerStore()
const message = useMessage()

const tab = ref<'expense' | 'income'>('expense')
const amount = ref<string>('0')
const category = ref('餐饮')
const note = ref('')

const expenseCats = ['餐饮', '交通', '日用品', '医疗', '教育', '娱乐', '房贷/房租', '红包', '其他']
const incomeCats = ['工资', '奖金', '兼职', '理财', '红包', '其他']
const cats = computed(() => (tab.value === 'expense' ? expenseCats : incomeCats))

function press(k: string) {
  if (k === 'del') {
    amount.value = amount.value.length > 1 ? amount.value.slice(0, -1) : '0'
    return
  }
  if (k === '.') {
    if (amount.value.includes('.')) return
    amount.value += '.'
    return
  }
  if (amount.value === '0' && k !== '.') amount.value = k
  else amount.value += k
}

async function save() {
  const n = Number(amount.value)
  if (!Number.isFinite(n) || n <= 0) {
    message.warning('请输入正确金额')
    return
  }
  const d = new Date()
  const at = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(
    d.getHours(),
  ).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  await ledgerStore.addLedger({
    type: tab.value,
    amount: n,
    category: category.value,
    note: note.value || '—',
    at,
  })
  message.success('记账成功')
  amount.value = '0'
  note.value = ''
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.push({ name: 'home' })">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">记账</div>
      <NButton quaternary circle @click="router.push({ name: 'ledger-stats' })">
        <template #icon><NIcon :component="AnalyticsOutline" /></template>
      </NButton>
    </div>

    <NSpace justify="space-between" style="width: 100%">
      <NButton size="small" secondary @click="router.push({ name: 'ledger-budget' })">预算</NButton>
    </NSpace>

    <NTabs v-model:value="tab" type="segment" animated>
      <NTabPane name="expense" tab="支出" />
      <NTabPane name="income" tab="收入" />
    </NTabs>

    <NCard class="glass amt" :bordered="false" :class="tab">
      <div class="label">金额</div>
      <div class="value">¥ {{ amount }}</div>
    </NCard>

    <div class="cat-scroll">
      <NButton
        v-for="c in cats"
        :key="c"
        size="small"
        round
        :type="category === c ? (tab === 'expense' ? 'error' : 'success') : 'default'"
        secondary
        @click="category = c"
      >
        {{ c }}
      </NButton>
    </div>

    <NInput v-model:value="note" placeholder="备注，如：早餐、地铁费" />

    <div class="keys">
      <NButton v-for="k in ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', 'del']" :key="k" class="key" secondary @click="press(k === 'del' ? 'del' : k)">
        {{ k === 'del' ? '⌫' : k }}
      </NButton>
    </div>

    <NButton block size="large" :type="tab === 'expense' ? 'error' : 'success'" @click="save">保存</NButton>
  </div>
</template>

<style scoped>
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 6px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
}
.amt {
  text-align: center;
  border-radius: 16px;
}
.amt.expense .value {
  color: #fb7185;
}
.amt.income .value {
  color: #4ade80;
}
.label {
  font-size: 13px;
  color: var(--fm-text-muted);
}
.value {
  font-size: 40px;
  font-weight: 800;
  margin-top: 6px;
}
.cat-scroll {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.keys {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  max-width: 720px;
}
.key {
  height: 52px;
}

.cat-scroll {
  gap: 10px;
}
</style>
