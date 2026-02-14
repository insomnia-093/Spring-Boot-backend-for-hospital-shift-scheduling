<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <div class="empty-title">{{ title }}</div>
    <div v-if="description" class="empty-description">{{ description }}</div>
    <button v-if="actionText" class="btn secondary" @click="$emit('action')" style="margin-top: 1rem;">
      {{ actionText }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    icon: {
      type: String,
      default: '📭'
    },
    title: {
      type: String,
      required: true
    },
    description: {
      type: String,
      default: ''
    },
    actionText: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped>
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--text-muted);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text);
}

.empty-description {
  font-size: 0.9rem;
  margin-bottom: 1rem;
}
</style>
