<template>
    <div class="header-container">
        <t-head-menu theme="light">
            <template #logo>
                <img width="230" src="@/assets/logo.png" />
            </template>

            <HeaderSearch v-if="isLoggedIn && visSearch"></HeaderSearch>

            <template #operations>
                <HeaderOperation :isLoggedIn="isLoggedIn" :userInfo="userInfo"></HeaderOperation>
            </template>


        </t-head-menu>
    </div>
</template>

<script>
import HeaderSearch from '@/components/Header/HeaderSearch'
import HeaderOperation from '@/components/Header/HeaderOperation'

export default {
    name: 'Header',
    props: ['isLoggedIn', 'userInfo', 'visSearch'],
    components: {
        HeaderSearch,
        HeaderOperation
    },
    methods: {
    }
}
</script>

<style lang="less" scoped>
.header-container {
    min-width: 1238px;
    box-shadow: 0px 2px 5px rgb(196, 196, 196);
}

/deep/ .t-menu__logo:not(:empty) {
    margin-right: 0px !important;
}
</style>