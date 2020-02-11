app.controller("itemController",function($scope){
	
	$scope.num = 1;

	// 数量加减
	$scope.addNum = function(x){
		$scope.num += x;
		if( $scope.num < 1 ){
			$scope.num = 1;
		}
	}

	/*
		规格选项
	*/
	$scope.specs = {};

	// 1. 规格选中
	$scope.selectSpecs = function(key,value){
		$scope.specs[key] = value;
		searchSku();
	}

	// 2. 选中规格样式
	$scope.specIsSelected = function(key,value){
		if($scope.specs[key] == value){
			return true;
		}
		return false;
	}

	// 3. 读取默认值
	$scope.loadSku = function(){
		$scope.sku=skuList[0];	
		$scope.specs = JSON.parse(JSON.stringify($scope.sku.spec));
	}

	// 4. 规格的联动效果
	// 4.1 定义函数，用于比较两个json对象是否相等
	matchjson = function(obj1,obj2){
		for(var key in obj1){
			if(obj1[key] != obj2[key]){
				return false;
			}
		}

		for(var key in obj2){
			if(obj2[key] != obj1[key]){
				return false;
			}
		}
		return true;
	}
	// 4.2 定义查询函数
	searchSku = function(){
		for( var i = 0 ; i < skuList.length ; i ++ ){
			if( matchjson(skuList[i].spec,$scope.specs) ){
				$scope.sku = skuList[i];
				return;
			}
		}
		$scope.sku = {id:-1,title:'该规格没有商品',price:-1,spec:{}};
	}

	//添加商品到购物车
	$scope.addToCart=function(){
		alert('skuid:'+$scope.sku.id);				
	}

	
});