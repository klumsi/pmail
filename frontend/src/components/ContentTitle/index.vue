<template>
    <div class="content-title-container">
        <div v-if="count">{{ title + ' · ' + count }}</div>
        <div v-else>{{ title }}</div>
    </div>
</template>

<script>
export default {
    props: ['title', 'count']
}
</script>

<style lang="less" scoped>
.content-title-container {
    padding: 20px 0px;
    font-size: 16px;
    font-weight: 500;
}
</style>
