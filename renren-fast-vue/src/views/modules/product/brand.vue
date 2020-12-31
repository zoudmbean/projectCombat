<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" placeholder="参数名" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">查询</el-button>
        <el-button v-if="isAuth('product:brand:save')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
        <el-button v-if="isAuth('product:brand:delete')" type="danger" @click="deleteHandle()" :disabled="dataListSelections.length <= 0">批量删除</el-button>
      </el-form-item>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;">
      <el-table-column
        type="selection"
        header-align="center"
        align="center"
        width="50">
      </el-table-column>
      <el-table-column
        prop="brandId"
        header-align="center"
        align="center"
        label="品牌id">
      </el-table-column>
      <el-table-column
        prop="name"
        header-align="center"
        align="center"
        label="品牌名">
      </el-table-column>
      <el-table-column
        prop="logo"
        header-align="center"
        align="center"
        label="品牌logo地址">
        <template slot-scope="scope">
          <!--<el-image
            style="width: 100px; height: 80px"
            fit="fill"
            :src="scope.row.logo" >
          </el-image>-->
          <img :src="scope.row.logo" style="width: 100px; height: 100px">
        </template>
      </el-table-column>
      <el-table-column
        prop="descript"v
        header-align="center"
        align="center"
        label="介绍">
      </el-table-column>
      <el-table-column
        prop="showStatus"
        header-align="center"
        align="center"
        label="显示状态">
        <template slot-scope="scope">
          <!--<span style="margin-left: 10px">{{ scope.row.date }}</span>-->
          <el-switch
            style="margin-left: 10px"
            v-model="scope.row.showStatus"
            :active-value="1"
            :active-text="scope.row.showStatus==1?'显示':' '"
            :inactive-text="scope.row.showStatus==0?'不显示':' '"
            :inactive-value="0"
            active-color="#13ce66"
            :width="60"
            inactive-color="#ff4949"
            @change="updateStatus(scope.row)"
          >
          </el-switch>
        </template>
      </el-table-column>
      <el-table-column
        prop="firstLetter"
        header-align="center"
        align="center"
        label="检索首字母">
      </el-table-column>
      <el-table-column
        prop="sort"
        header-align="center"
        align="center"
        label="排序">
      </el-table-column>
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="200"
        label="操作">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="generateBrandAndCategoryHandle(scope.row.brandId)">关联分类</el-button>
          <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.brandId)">修改</el-button>
          <el-button type="text" size="small" @click="deleteHandle(scope.row.brandId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="pageIndex"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper">
    </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>

    <!-- 关联分类 -->
    <el-dialog title="分类关联" :visible.sync="dialogTableVisible">
      <div>
        <!-- 新增分类 -->
        <el-popover placement="right" width="400" trigger="click" v-model="popCatelogSelectVisible">
          <el-cascader
            v-model="catePath"
            :options="categorys"
            filterable
            placeholder="试试搜索：手机"
            :props="props">
          </el-cascader>
          <div style="text-align: right; margin: 0">
            <el-button size="mini" type="text" @click="popCatelogSelectVisible=false">取消</el-button>
            <el-button type="primary" size="mini" @click="addCatelogSelect">确定</el-button>
          </div>
          <el-button slot="reference" type="error" icon="el-icon-folder-add" @click="catePath=[]">新增关联</el-button>
        </el-popover>
      </div>
      <el-table :data="brandAndCategorys">
        <el-table-column prop="id" label="#"></el-table-column>
        <el-table-column prop="brandName" label="品牌名"></el-table-column>
        <el-table-column prop="catelogName" label="分类名"></el-table-column>
        <el-table-column fixed="right" header-align="center" align="center" label="操作">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="deleteCateRelationHandle(scope.row.id,scope.row.brandId)"
            >移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogTableVisible = false">取 消</el-button>
        <el-button type="primary" @click="dialogTableVisible = false">确 定</el-button>
      </span>
    </el-dialog>


  </div>
</template>

<script>
  import AddOrUpdate from './brand-add-or-update'
  export default {
    data () {
      return {
        dataForm: {
          key: ''
        },
        props:{
          value:'catId',
          label:'name',
          children:'children'
        },
        brandAndCategorys:[],
        categorys:[],     // 所有的分类
        catePath: [],
        dialogTableVisible:false,   // 关联分类dialog
        popCatelogSelectVisible:false,
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalPage: 0,
        dataListLoading: false,
        dataListSelections: [],
        relativeBrandId:'',
        addOrUpdateVisible: false
      }
    },
    components: {
      AddOrUpdate
    },
    activated () {
      this.getDataList()
    },
    methods: {
      // 删除关联关系
      deleteCateRelationHandle(id,brandId){
        this.$http({
          url: this.$http.adornUrl(`/product/categorybrandrelation/delete`),
          method: 'post',
          data:[id]
        }).then(({data}) => {
          this.initBrandAndCategoryDatas(brandId);
          this.$message({
            message: '删除成功',
            type: 'success',
            duration: 1500,
            onClose: () => {
              // this.getDataList()
            }
          })
        })
      },
      addCatelogSelect(){
        // 分类ID
        this.$http({
          url: this.$http.adornUrl('/product/brand/categorybrandrelation/save'),
          data:{brandId:this.relativeBrandId,catelogId:this.catePath[2]},
          method: 'post'
        }).then(({data,status,statusText}) => {
          if('OK' === statusText){
            this.$message({
              message: '关联成功',
              type: 'success',
              duration: 1500,
              onClose: () => {
                // this.getDataList()
              }
            })
            this.initBrandAndCategoryDatas(this.relativeBrandId);
            this.popCatelogSelectVisible=false;
          }
        })
      },
      // 关联分类
      generateBrandAndCategoryHandle(brandId){
        this.dialogTableVisible = true;
        this.relativeBrandId = brandId;
        this.getAllCategorys();
        this.initBrandAndCategoryDatas(brandId);
      },
      initBrandAndCategoryDatas(brandId){
        this.$http({
          url: this.$http.adornUrl(`/product/categorybrandrelation/list/${brandId}`),
          method: 'get'
        }).then(({data}) => {
          if(data.code * 1 === 0){
            this.brandAndCategorys = data.data
          }
        })
      },
      getAllCategorys(){
        this.$http({
          url: this.$http.adornUrl('/product/category/list/tree'),
          method: 'get'
        }).then(({data}) => {
          if(data.code * 1 === 0){
            this.categorys = data.data
          }
        })
      },
      // 更新显示状态
      updateStatus(brandData){
        // console.log(data)
        let {brandId,showStatus} = brandData;
        this.$http({
          url: this.$http.adornUrl('/product/brand/update/status'),
          method: 'post',
          data: this.$http.adornData({brandId,showStatus}, false)
        }).then(({data}) => {
          if (!data || data.code != 0) {
            this.$message.error(data.msg)
          }
        })
      },
      // 获取数据列表
      getDataList () {
        this.dataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/product/brand/list'),
          method: 'get',
          params: this.$http.adornParams({
            'page': this.pageIndex,
            'limit': this.pageSize,
            'key': this.dataForm.key
          })
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.dataList = data.page.list
            this.totalPage = data.page.totalCount
          } else {
            this.dataList = []
            this.totalPage = 0
          }
          this.dataListLoading = false
        })
      },
      // 每页数
      sizeChangeHandle (val) {
        this.pageSize = val
        this.pageIndex = 1
        this.getDataList()
      },
      // 当前页
      currentChangeHandle (val) {
        this.pageIndex = val
        this.getDataList()
      },
      // 多选
      selectionChangeHandle (val) {
        this.dataListSelections = val
      },
      // 新增 / 修改
      addOrUpdateHandle (id) {
        this.addOrUpdateVisible = true
        this.$nextTick(() => {
          this.$refs.addOrUpdate.init(id)
        })
      },
      // 删除
      deleteHandle (id) {
        var ids = id ? [id] : this.dataListSelections.map(item => {
          return item.brandId
        })
        this.$confirm(`确定对[id=${ids.join(',')}]进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/product/brand/delete'),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: '操作成功',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        })
      }
    }
  }
</script>
