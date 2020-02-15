app.controller("cartController",function($scope,cartService){
	
	// 查询列表
	$scope.findCartList = function() {
		cartService.findCartList().success(function(response) {
			$scope.cartlist = response;
			$scope.totalObject = cartService.sum($scope.cartlist);
		});
	}
	
	// 添加到购物车
	$scope.addGoodsToCartList = function(itemId,num) {
		cartService.addGoodsToCartList(itemId,num).success(function(response) {
			if(!response.success){
				alert(response.message);
			} else {
				// 添加成功，刷新列表
				$scope.findCartList();
			}
		});
	}
	
});