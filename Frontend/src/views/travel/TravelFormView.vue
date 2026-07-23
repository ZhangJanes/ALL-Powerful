<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { useTravelStore } from '@/stores/travel'
import { useMessage } from 'naive-ui'

const router = useRouter()
const travelStore = useTravelStore()
const message = useMessage()

const title = ref('')
const category = ref('周末出游')
const place = ref('')
const companions = ref('')
const transport = ref('自驾')
const remark = ref('')
const remindEnabled = ref(false)
const remindMinutesBefore = ref<number | null>(60)
const now = Date.now()
const start = ref<number | null>(now)
const end = ref<number | null>(now + 60 * 60 * 1000)

function formatTsToDateTime(ts: number) {
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}

async function save() {
  if (!title.value.trim() || !place.value.trim()) {
    message.warning('请填写标题与地点')
    return
  }
  if (!start.value || !end.value) {
    message.warning('请选择开始和结束时间')
    return
  }
  if (end.value < start.value) {
    message.warning('结束时间不能早于开始时间')
    return
  }
  await travelStore.saveTripToServer({
    title: title.value,
    category: category.value,
    start: formatTsToDateTime(start.value),
    end: formatTsToDateTime(end.value),
    place: place.value,
    companions: companions.value,
    transport: transport.value,
    remark: remark.value,
    remindEnabled: remindEnabled.value,
    remindMinutesBefore: remindEnabled.value ? (remindMinutesBefore.value || 60) : undefined,
    done: false,
    checklist: [],
  })
  message.success('已保存行程')
  router.replace({ name: 'travel' })
}
</script>

<template>
  <div class="app-shell page">
    <div class="nav glass">
      <NButton quaternary circle @click="router.back()">
        <template #icon><NIcon :component="ArrowBackOutline" /></template>
      </NButton>
      <div class="page-title">创建出行</div>
      <NButton type="primary" text @click="save">保存</NButton>
    </div>

    <NForm label-placement="top">
      <NFormItem label="标题">
        <NInput v-model:value="title" placeholder="如：周末带孩子去公园" />
      </NFormItem>
      <NFormItem label="分类">
        <NSelect
          v-model:value="category"
          :options="['日常通勤', '周末出游', '旅行', '回老家', '办事'].map((v) => ({ label: v, value: v }))"
        />
      </NFormItem>
      <NFormItem label="开始">
        <NDatePicker
          v-model:value="start"
          type="datetime"
          clearable
          format="yyyy-MM-dd HH:mm:ss"
          style="width: 100%"
        />
      </NFormItem>
      <NFormItem label="结束">
        <NDatePicker
          v-model:value="end"
          type="datetime"
          clearable
          format="yyyy-MM-dd HH:mm:ss"
          style="width: 100%"
        />
      </NFormItem>
      <NFormItem label="地点">
        <NInput v-model:value="place" placeholder="可关联地图（演示）" />
      </NFormItem>
      <NFormItem label="同行人物">
        <NInput v-model:value="companions" placeholder="如：家人、朋友" />
      </NFormItem>
      <NFormItem label="交通方式">
        <NSelect
          v-model:value="transport"
          :options="['步行', '公交', '地铁', '自驾', '高铁', '飞机'].map((v) => ({ label: v, value: v }))"
        />
      </NFormItem>
      <NFormItem label="备注">
        <NInput v-model:value="remark" placeholder="如：带身份证、雨伞" />
      </NFormItem>
      <NFormItem label="出发前提醒">
        <NSpace align="center">
          <NSwitch v-model:value="remindEnabled" />
          <NInputNumber v-if="remindEnabled" v-model:value="remindMinutesBefore" :min="1" :max="1440">
            <template #suffix>分钟</template>
          </NInputNumber>
        </NSpace>
      </NFormItem>
    </NForm>
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
</style>
