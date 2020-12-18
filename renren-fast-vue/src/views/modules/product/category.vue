<template>
  <div>
  <el-tree
    :data="menus"
    :props="defaultProps"
    show-checkbox
    node-key="catId"
    :expand-on-click-node="false"
    :highlight-current="true"
    show-checkbox
    accordion
    :default-expanded-keys="expandedKeys"
    @node-click="handleNodeClick">

    <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <i v-if="node.level < 3" class="el-icon-circle-plus-outline" style="color: green;" @click="() => append(data)" ></i>
          <i class="el-icon-edit" style="color:darkorange;" @click="editCategory(data)" ></i>
          <i v-if="node.childNodes.length==0" class="el-icon-delete" style="color:darkred;" @click="() => remove(node, data)"></i>
        </span>
      </span>
  </el-tree>

    <!-- 对话框 -->
  <el-dialog :title="title" :visible.sync="dialogFormVisible">
    <el-form :model="category">
      <el-form-item label="分类名称">
        <el-input v-model="category.name" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="计量单位">
        <el-input v-model="category.productUnit" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="分类图标">
        <el-input v-model="category.icon" autocomplete="off"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogFormVisible = false">取 消</el-button>
      <el-button type="primary" @click="addCategory" v-if="!btnType">保存</el-button>
      <el-button type="primary" @click="updateCategory" v-else>更新</el-button>
    </div>
  </el-dialog>

  </div>
</template>

<script>
    export default {
        name: "Category",
        data() {
          return {
            title:'',   // 对话框标题
            category:{name:'',parentCid:'',sort:0,showStatus:1,catLevel:'',catId : null,productUnit:'',icon:''},              // 节点对象
            dialogFormVisible:false,  // 对话框是否显示
            expandedKeys:[],    // 默认展开的节点
            menus: [],
            defaultProps: {
              children: 'children',
              label: 'name'
            }
          };
        },
        computed:{
            btnType(){
              return this.category['catId'] != null;
            }
        },
        methods: {
          getCategoryByCatId(id){    // 获取分类对象
            return  new Promise((resolve, reject) => {
              this.$http({
                url: this.$http.adornUrl(`/product/category/info/${id}`),
                method: 'get'
              }).then(data => {
                resolve(data);
              })
            });
          },
          updateCategory(){     // 更新分类到数据库
            const {catId,icon,name,productUnit} = this.category;
            let promise = new Promise((resolve, reject) => {
              this.$http({
                url: this.$http.adornUrl(`/product/category/update`),
                data: this.$http.adornData({catId,icon,name,productUnit}, false),
                method: 'post'
              }).then(data => {
                resolve(data);
              })
            });

            promise.then(data => {
              if("OK" === data.statusText){
                // 提示窗口
                this.$message({
                  message: '更新成功',
                  type: 'success',
                  duration: 1500
                })

                // 1. 刷新菜单
                this.getMenus();
                // 2. 关闭对话框
                this.dialogFormVisible = false;
                // 3. 展开当前菜单
                this.expandedKeys = [ this.category.parentCid];

                // 重置表单数据
                this.resetCategory();
              } else {
                // 提示窗口
                this.$message({
                  message: '更新失败',
                  type: 'error',
                  duration: 1500
                })

                return;
              }

            })

          },
          editCategory(categoryData){   // 编辑节点
            this.title = "编辑分类";

            // 1. 赋值（查询数据库）
            let promise = this.getCategoryByCatId(categoryData.catId);

            promise.then(({data,statusText}) => {
              const {catId,icon,name,parentCid,productUnit} = data.category;
              // 2. 回显数据
              this.category.catId = catId;
              this.category.parentCid = parentCid;
              this.category.name = name;
              this.category.icon = icon;
              this.category.productUnit = productUnit;
            })

            // 打开对话框
            this.dialogFormVisible = true;
          },
          addCategory(){      // 添加节点
            this.$confirm(`确定添加【 ${this.category.name} 】菜单吗？`, '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.$http({
                url: this.$http.adornUrl('/product/category/save'),
                method: 'post',
                data: this.$http.adornData(this.category, false)
              }).then(({data}) => {
                if (data && data.code === 0) {

                  // 提示窗口
                  this.$message({
                    message: '添加成功',
                    type: 'success',
                    duration: 1500
                  })

                  // 1. 刷新菜单
                  this.getMenus();
                  // 2. 关闭对话框
                  this.dialogFormVisible = false;
                  // 3. 展开当前菜单
                  this.expandedKeys = [ this.category.parentCid];

                } else {
                  this.$message.error(data.msg)
                }
              })
            }).catch(() => {})
          },
          append(data) {  // 添加节点
            this.title = "添加分类";

            // 打开对话框之前，将模态框值清空
            this.resetCategory();

            // 赋值  category:{name:'',parentCid:'',sort:0,showStatus:1,catLevel:''},
            this.category.parentCid = data.catId;
            this.category.catLevel = data.catLevel * 1 + 1;
            // 打开对话框
            this.dialogFormVisible = true;
          },
          remove(node, data) {  // 删除节点
            console.log(node,data)
            var delIds = [data.catId];

            this.$confirm(`确定删除【 ${data.name} 】菜单吗？`, '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.$http({
                url: this.$http.adornUrl('/product/category/delete'),
                method: 'post',
                data: this.$http.adornData(delIds, false)
              }).then(({data}) => {
                if (data && data.code === 0) {

                  // 提示窗口
                  this.$message({
                    message: '操作成功',
                    type: 'success',
                    duration: 1500,
                    onClose: () => {

                    }
                  })

                  // 1. 刷新菜单
                  this.getMenus();
                  // 2. 展开当前菜单
                  this.expandedKeys = [node.data.parentCid];

                } else {
                  this.$message.error(data.msg)
                }
              })
            }).catch(() => {})

          },
          handleNodeClick(data) {  // 点击节点
            // console.log(data);
          },
          resetCategory(){    // 重置分类
            this.category = {name:'',parentCid:'',sort:0,showStatus:1,catLevel:'',catId:null,productUnit:'',icon:''}
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
