<template>
  <el-tree
    :data="menus"
    :props="defaultProps"
    show-checkbox
    node-key="catId"
    :expand-on-click-node="false"
    :highlight-current="true"
    show-checkbox
    accordion
    @node-click="handleNodeClick">

    <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button v-if="node.level < 3"
            type="text"
            size="mini"
            @click="() => append(data)">
            Append
          </el-button>
          <el-button v-if="node.childNodes.length==0"
            type="text"
            size="mini"
            @click="() => remove(node, data)">
            Delete
          </el-button>
        </span>
      </span>

  </el-tree>
</template>

<script>
    export default {
        name: "Category",
        data() {
          return {
            menus: [],
            defaultProps: {
              children: 'children',
              label: 'name'
            }
          };
        },
        methods: {
          append(data) {  // 添加节点

          },
          remove(node, data) {  // 删除节点
console.log(node.children)
          },
          handleNodeClick(data) {
            console.log(data);
          },
          getMenus(){
            this.$http({
              url: this.$http.adornUrl('/product/category/list/tree'),
              method: 'get'
            }).then(({data}) => {
              if(data.code * 1 === 0){
                this.menus = data.data
              }
            })
          }
        },
        //生命周期 - 创建完成（可以访问当前this实例）
        created() {
          this.getMenus();
        },
        //生命周期 - 挂载完成（可以访问DOM元素）
        mounted() {},
        beforeCreate() {}, //生命周期 - 创建之前
        beforeMount() {}, //生命周期 - 挂载之前
        beforeUpdate() {}, //生命周期 - 更新之前
        updated() {}, //生命周期 - 更新之后
        beforeDestroy() {}, //生命周期 - 销毁之前
        destroyed() {}, //生命周期 - 销毁完成
        activated() {} //如果页面有keep-alive缓存功能，这个函数会触发
    }
</script>

<style scoped>

</style>
