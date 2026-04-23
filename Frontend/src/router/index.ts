import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
        { path: 'messages', name: 'messages', component: () => import('@/views/MessagesView.vue') },
        { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue') },
        { path: 'more', name: 'more', component: () => import('@/views/MoreView.vue') },
        { path: 'memo', name: 'memo', component: () => import('@/views/memo/MemoListView.vue') },
        { path: 'memo/new', name: 'memo-new', component: () => import('@/views/memo/MemoEditView.vue') },
        { path: 'memo/:id', name: 'memo-edit', component: () => import('@/views/memo/MemoEditView.vue') },
        { path: 'ledger', name: 'ledger', component: () => import('@/views/ledger/LedgerView.vue') },
        { path: 'ledger/budget', name: 'ledger-budget', component: () => import('@/views/ledger/BudgetView.vue') },
        { path: 'ledger/stats', name: 'ledger-stats', component: () => import('@/views/ledger/LedgerStatsView.vue') },
        { path: 'habits', name: 'habits', component: () => import('@/views/habits/HabitsView.vue') },
        { path: 'habits/new', name: 'habits-new', component: () => import('@/views/habits/HabitFormView.vue') },
        { path: 'habits/:id', name: 'habits-detail', component: () => import('@/views/habits/HabitDetailView.vue') },
        { path: 'travel', name: 'travel', component: () => import('@/views/travel/TravelListView.vue') },
        { path: 'travel/new', name: 'travel-new', component: () => import('@/views/travel/TravelFormView.vue') },
        { path: 'travel/:id', name: 'travel-detail', component: () => import('@/views/travel/TravelDetailView.vue') },
        { path: 'ideas', name: 'ideas', component: () => import('@/views/ideas/IdeasListView.vue') },
        { path: 'ideas/new', name: 'ideas-new', component: () => import('@/views/ideas/IdeaEditView.vue') },
        { path: 'ideas/:id', name: 'ideas-detail', component: () => import('@/views/ideas/IdeaDetailView.vue') },
        { path: 'ideas/:id/edit', name: 'ideas-edit', component: () => import('@/views/ideas/IdeaEditView.vue') },
        { path: 'photos', name: 'photos', component: () => import('@/views/photos/PhotosView.vue') },
        { path: 'guide', name: 'guide', component: () => import('@/views/guide/GuideHomeView.vue') },
        { path: 'guide/:category', name: 'guide-list', component: () => import('@/views/guide/GuideListView.vue') },
        { path: 'guide/:category/:slug', name: 'guide-detail', component: () => import('@/views/guide/GuideDetailView.vue') },
      ],
    },
  ],
})

export default router
