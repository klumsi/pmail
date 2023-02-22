<template>
    <div class="header-search">
        <t-input placeholder="搜索" @change="submit" v-model.trim="q">
            <template #prefix-icon>
                <SearchIcon size="18"/>
            </template>
        </t-input>
    </div>
</template>

<script>
import bus from '@/EventBus'
import { SearchIcon } from 'tdesign-icons-vue';

export default {
    name: "Search",
    data() {
        return {
            q: "",
            refreshSearch: false
        };
    },
    methods: {
        submit() {
            bus.$emit("search", this.q);
        },
    },
    created() {
        bus.$on("search", val => {
            this.q = val;
        });
    },
    components: { SearchIcon }
}
</script>

<style lang="less" scoped>
/deep/ .t-input {
    width: 300px;
    background-color: #f5f6f7;
    border-style: none;

    &:focus-within {
        background-color: white;
    }
}
</style>