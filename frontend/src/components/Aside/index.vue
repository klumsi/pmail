<template>
    <div class="aside-container">
        <t-menu :defaultValue="active">
            <t-menu-item value="/compose" to="/compose">
                <t-icon name="edit-1"></t-icon>
                &nbsp;写邮件
            </t-menu-item>
            <t-menu-item value="/contacts" to="/contacts">
                <t-icon name="usergroup"></t-icon>
                &nbsp;通讯录
            </t-menu-item>
            <t-menu-item value="/inbox" to="/inbox">
                <t-icon name="mail"></t-icon>
                &nbsp;收件箱
            </t-menu-item>
            <t-menu-item value="/sent" to="/sent">
                <t-icon name="check-circle"></t-icon>
                &nbsp;已发送
            </t-menu-item>
            <t-menu-item value="/drafts" to="/drafts">
                <t-icon name="root-list"></t-icon>
                &nbsp;草稿箱
            </t-menu-item>
            <t-menu-item value="/archive" to="/archive">
                <t-icon name="control-platform"></t-icon>
                &nbsp;归档
            </t-menu-item>
            <t-menu-item value="/trash" to="/trash">
                <t-icon name="delete"></t-icon>
                &nbsp;已删除
            </t-menu-item>
            <t-menu-item value="/junk" to="/junk">
                <t-icon name="filter-clear"></t-icon>
                &nbsp;垃圾邮件
            </t-menu-item>
        </t-menu>
    </div>
</template>

<script>
export default {
    props: ['active'],
}
</script>

<style lang="less" scoped>
.aside-container {
    height: 100%;
}

.t-default-menu {
    border: 6px solid #f0f0f0;
}
</style>