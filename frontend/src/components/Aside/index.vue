<template>
    <div class="aside-container">
        <t-menu :defaultValue="active">
            <t-menu-item value="/compose" to="/compose">
                <Edit1Icon/>
                &nbsp;写邮件
            </t-menu-item>
            <t-menu-item value="/contacts" to="/contacts">
                <UsergroupIcon/>
                &nbsp;通讯录
            </t-menu-item>
            <t-menu-item value="/inbox" to="/inbox">
                <MailIcon/>
                &nbsp;收件箱
            </t-menu-item>
            <t-menu-item value="/sent" to="/sent">
                <CheckCircleIcon/>
                &nbsp;已发送
            </t-menu-item>
            <t-menu-item value="/drafts" to="/drafts">
                <RootListIcon/>
                &nbsp;草稿箱
            </t-menu-item>
            <t-menu-item value="/archive" to="/archive">
                <ControlPlatformIcon/>
                &nbsp;归档
            </t-menu-item>
            <t-menu-item value="/trash" to="/trash">
                <DeleteIcon/>
                &nbsp;已删除
            </t-menu-item>
            <t-menu-item value="/junk" to="/junk">
                <FilterClearIcon/>
                &nbsp;垃圾邮件
            </t-menu-item>
        </t-menu>
    </div>
</template>

<script>
import { Edit1Icon, UsergroupIcon, MailIcon, CheckCircleIcon, RootListIcon, ControlPlatformIcon, DeleteIcon, FilterClearIcon } from 'tdesign-icons-vue';

export default {
    props: ['active'],
    components: {
        Edit1Icon,
        UsergroupIcon,
        MailIcon,
        CheckCircleIcon,
        RootListIcon,
        ControlPlatformIcon,
        DeleteIcon,
        FilterClearIcon,
    }
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