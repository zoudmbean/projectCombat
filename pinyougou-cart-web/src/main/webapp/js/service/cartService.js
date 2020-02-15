app.service("cartService",function($http){
	
	this.findCartList = function() {
		return $http.get("/cart/findCartList.do");
	}
	
	this.addGoodsToCartList = function(itemId,num) {
		return $http.get("/cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num);
	}
	
	this.sum = function(cartlist) {
		var totalObject = {totalNum:0,totalMoney:0};
		for(var i = 0 ; i < cartlist.length ; i ++){
			var item = cartlist[i];	// 购物车对象
			for(var j = 0 ; j < item.orderItemList.length; j++){
				var orderItem = item.orderItemList[j];// 购物车明细
				totalObject.totalNum += orderItem.num;// 累加数量
				totalObject.totalMoney += orderItem.totalFee;// 累加金额
			}
		}
		return totalObject;
	}
	
});