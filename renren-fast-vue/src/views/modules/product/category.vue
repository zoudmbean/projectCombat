<template>
  <div>
    <el-switch
      v-model="draggable"
      active-color="#13ce66"
      inactive-color="#ff4949"
      active-text="关闭拖拽"
      inactive-text="开启拖拽">
    </el-switch>
    <el-button type="warning" round icon="el-icon-chec" size="small" v-show="draggable" @click="batchSave">批量保存</el-button>
    <el-button type="danger" round icon="el-icon-delete" size="small" @click="batchDel">批量删除</el-button>
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
      @node-click="handleNodeClick"
      @node-drop="handleDrop"
      :draggable="draggable"
      :allow-drop="allowDrop"
      ref="menuTree"
    >
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
            draggable:false,      // 是否拖拽  默认否
            pCid: [],             // 父ID
            updateNodes:[],     // 要修改的所有拖拽节点
            maxLevel:1, // 最深层级
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
          batchDel(){
            let checkedNodes = this.$refs.menuTree.getCheckedNodes();
            if(!checkedNodes || checkedNodes.length == 0){
              this.$message({
                message: '请选择需要删除的菜单节点！',
                type: 'error',
                duration: 1500
              })
              return ;
            }
            // 获取选中的ID
            let ids = checkedNodes.map(node => {
              return node.catId
            });

            // 删除
            this.$confirm(`确定批量删除【${checkedNodes.map(node => node.name)}】菜单吗？`, '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.$http({
                url: this.$http.adornUrl('/product/category/delete'),
                method: 'post',
                data: this.$http.adornData(ids, false)
              }).then(({data}) => {
                if (data && data.code === 0) {

                  // 提示窗口
                  this.$message({
                    message: '批量删除成功',
                    type: 'success',
                    duration: 1500
                  })

                  // 1. 刷新菜单
                  this.getMenus();

                } else {
                  this.$message.error(data.msg)
                }
              })
            }).catch(() => {})
          },
          // ********************************************   1. 拖拽相关方法
          batchSave(){
            // 4. 保存到数据库
            this.$http({
              url: this.$http.adornUrl(`/product/category/update/sorts`),
              method: 'post',
              data:this.updateNodes
            }).then(data => {
              if("OK" === data.statusText){
                // 提示窗口
                this.$message({
                  message: '拖拽更新成功！',
                  type: 'success',
                  duration: 1500
                })

                // 1. 刷新菜单
                this.getMenus();
                // 2. 重置数据
                this.updateNodes = [];
                this.maxLevel = 1;
                // 3. 展开所有的父菜单
                this.expandedKeys = this.pCid;

              } else {
                // 提示窗口
                this.$message({
                  message: '拖拽更新数据库失败',
                  type: 'error',
                  duration: 1500
                })

                return;
              }
            })
          },
          /*
          * 拖拽时判定目标节点能否被放置.
          * 参数：
          *   draggingNode：被拖拽节点对应的 Node
          *   dropNode：结束拖拽时最后进入的节点
          *   type：拖拽到的目标位置。type 参数有三种情况：'prev'、'inner' 和 'next'，分别表示放置在目标节点前、插入至目标节点和放置在目标节点后
          * */
          // 规定树形菜单最大层级为3层，所以，这里不能随便拖放
          allowDrop(draggingNode, dropNode, type){
            //1、被拖动的当前节点以及所在的父节点总层数不能大于3

            //1）、被拖动的当前节点总层数
            console.log("allowDrop",draggingNode, dropNode, type);
            this.countNodeLevel(draggingNode);
            console.log("this.maxLevel = " + this.maxLevel);

            // 当前被拖拽的节点深度 = 最大深度 - 当前节点的lever + 1
            let deep = 1;
            if(this.maxLevel > 1){
              deep = Math.abs(this.maxLevel - draggingNode.level) + 1;
            }
            console.log("深度：", deep);

            //当前正在拖动的节点+父节点所在的深度不大于3即可
            if(type == 'inner'){
              return deep + dropNode.level <= 3;
            } else {
              return deep + dropNode.parent.level <= 3;
            }
          },
          countNodeLevel(node){    // 获取被拖拽节点的最大深度
            if(node.childNodes != null && node.childNodes.length > 0){
              node.childNodes.forEach(child => {
                if(child.level > this.maxLevel){
                  this.maxLevel = child.level;
                }
                this.countNodeLevel(child);
              })
            }
          },
          /*
          * 拖拽成功完成时触发的事件;
          * 共四个参数：
          *   draggingNode：被拖拽节点对应的 Node
          *   dropNode：结束拖拽时最后进入的节点
          *   dropType：被拖拽节点的放置位置（before、after、inner）
          *   event：event
          * */
          handleDrop(draggingNode, dropNode, dropType, event){
            console.log("handleDrop",draggingNode, dropNode, dropType);
            // 用于收集需要修改的节点（拖拽之后，需要修改的字段有   层级level   parentID  sort  ）
            //1、当前节点最新的父节点id
            let pCid = 0;
            // 2. 子节点
            let siblings = null;
            if (dropType == "before" || dropType == "after") {  // 如果节点类型不是inner  那么父节点就是dropNode的父节点
              pCid = dropNode.parent.data.catId || 0;
              siblings = dropNode.parent.childNodes;
            } else {                                            // 如果是inner，那么父节点就是dropNode
              pCid = dropNode.data.catId;
              siblings = dropNode.childNodes;
            }
            this.pCid.push(pCid);

            //2、当前拖拽节点的最新顺序，
            for (let i = 0; i < siblings.length; i++) {
              if (siblings[i].data.catId == draggingNode.data.catId) {   //如果遍历的是当前正在拖拽的节点
                let catLevel = draggingNode.level;
                if (siblings[i].level != draggingNode.level) {
                  //当前节点的层级发生变化
                  catLevel = siblings[i].level;
                  //修改他子节点的层级
                  this.updateChildNodeLevel(siblings[i]);
                }
                this.updateNodes.push({
                  catId: siblings[i].data.catId,
                  sort: i,
                  parentCid: pCid,
                  catLevel: catLevel
                });
              } else {
                this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
              }
            }
            //3、当前拖拽节点的最新层级
            console.log("updateNodes", this.updateNodes);
            // 更新数据库
            // 删除了更新数据库的操作，改为通过点击按钮的形式
          },
          updateChildNodeLevel(node) {
            if (node.childNodes.length > 0) {
              for (let i = 0; i < node.childNodes.length; i++) {
                var cNode = node.childNodes[i].data;
                this.updateNodes.push({
                  catId: cNode.catId,
                  catLevel: node.childNodes[i].level
                });
                this.updateChildNodeLevel(node.childNodes[i]);
              }
            }
          },
          // ********************************************   2. 下面是常规方法
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
