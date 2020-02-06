 //控制层 
app.controller('goodsController' ,function($scope,$controller,uploadService,goodsService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.add=function(){		
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert("添加成功！");
					$scope.entity = {};
					// 情况富文本编辑器的内容
					editor.html('');
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	$scope.uploadFile = function() {
		uploadService.uploadFile().success(function(res) {
			if(res.success){
				$scope.imgEntity.url = res.message;
			} else{
				alert(res.message);
			}
		}).error(function(){
			alert("上传文件出错！");
		});
	}
	
	// 定义组合对象结构  goodsDesc.specificationItems
	$scope.entity = {goodsDesc:{itemImages:[],specificationItems:[]}};
	$scope.addImgToList = function() {
		$scope.entity.goodsDesc.itemImages.push($scope.imgEntity);
	}
	
	// 移除图片
	$scope.removeImgEntity = function(index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	/*
	 * 实现商品分类下拉列表方法
	 * */
	// 1. 一级下拉列表
	$scope.selectItemCatList = function() {
		itemCatService.findByParentId(0).success(function(data) {
			$scope.itemCat1List = data;
		});
	}
	
	$scope.$watch('entity.goods.category1Id',function(newValue,oldvalue){
		if(newValue){
			itemCatService.findByParentId(newValue).success(function(data) {
				$scope.itemCat2List = data;
				$scope.entity.goods.typeTemplateId = '';
			});
		}
	});
	
	$scope.$watch('entity.goods.category2Id',function(newValue,oldvalue){
		if(newValue){
			itemCatService.findByParentId(newValue).success(function(data) {
				$scope.itemCat3List = data;
				$scope.entity.goods.typeTemplateId = '';
			});
		}
	});
	
	// 读取模板信息
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {  
		if(newValue){
			itemCatService.findOne(newValue).success(
					function(response){
						$scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID    
					}
			);    
		}
    });
	
	// 模板id监听器
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {    
		
		// 获取品牌列表
		if(newValue){
			typeTemplateService.findOne(newValue).success(
				function(response){
					$scope.typeTemplate = response;
					$scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表   
					// 扩展属性
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
				}
			);  
			// 获取规格信息
			typeTemplateService.findSpecList(newValue).success(function(res) {
				$scope.specList = res;
			});
		}
		
    });
	
	// 更新规格列表
	$scope.updateSpecAttribute = function($event,name,value){
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
		// 如果存在
		if(object != null){
			// 然后判断当前点击是取消还是勾选
			if($event.target.checked){ // 选择，直接添加
				object.attributeValue.push(value);
			} else { // 否则删除元素
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				// 如果 object.attributeValue 没有元素了，那么从当前集合中删除object
				if(object.attributeValue.length === 0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		} else {
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	/**
	 * 生成sku列表
	 * */
	$scope.createItemList = function() {
		// 初始化列表
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];
		// [{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["128G"]}]
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i = 0 ; i < items.length ; i++){
			$scope.entity.itemList = caculateItenList($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
		
	}
	
	function caculateItenList(list,columnName,values) {
		var newList = [];
		// 循环集合
		for(var i = 0 ; i < list.length; i++){
			for(var j = 0 ; j < values.length; j++){
				// 深克隆原来的行
				var item = JSON.parse(JSON.stringify(list[i]));
				item.spec[columnName] = values[j];
				newList.push(item);
			}
		}
		return newList;
	}
    
});	
