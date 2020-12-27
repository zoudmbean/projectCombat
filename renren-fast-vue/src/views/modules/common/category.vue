<template>
  <div>
    <el-tree
      :data="menus"
      :props="defaultProps"
      node-key="catId"
      :expand-on-click-node="false"
      :highlight-current="true"
      accordion
      @node-click="handleNodeClick"
      ref="menuTree"
    >
    </el-tree>

  </div>
</template>

<script>
    import PubSub from 'pubsub-js'
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
      computed:{
      },
      methods: {
        handleNodeClick(data,node,component) {  // 点击节点
          // 发布消息(nodeClick)
          PubSub.publish('nodeClick', {data,node,component})
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
