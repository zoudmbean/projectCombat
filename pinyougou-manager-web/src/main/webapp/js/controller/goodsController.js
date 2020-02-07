 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,itemCatService){	
	
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
		goodsService.findPage($scope.seachEntity,page,rows).success(
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
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){		
		if($scope.selectIds && $scope.selectIds.length > 0){
			//获取选中的复选框			
			goodsService.deleGoods( $scope.selectIds ).success(
				function(response){
					if(response.success){
						$scope.reloadList();//刷新列表
						$scope.selectIds=[];
					}						
				}		
			);				
		} else {
			alert("请选择要删除的行！");
		}
	}
	
	/*
	 * 定义数组，记录状态信息
	 * */
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	
	// 查询分类信息
	$scope.itemCatList = [];
	$scope.getitemCatList = function(){
		itemCatService.findAll().success(function(response) {
			for(var i = 0 ; i < response.length ; i ++ ){
				$scope.itemCatList[response[i].id] = response[i].name;
			}
		});
	} 
	
	$scope.updateAuditStatus = function(status) {
		goodsService.updateAuditStatus($scope.selectIds,status).success(function(res) {
			if(res.success){
				alert(res.message);
				$scope.reloadList();//重新加载
				$scope.selectIds = [];
			} else {
				alert(res.message);
			}
		});
	}
    
});	
