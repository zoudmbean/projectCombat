 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	$scope.reg = function() {
		
		// 进行密码判断
		if($scope.password != $scope.entity.password){
			
			alert("两次密码输入不一致！");
			$scope.password = "";
			$scope.entity.password = "";
		}
		
		userService.add($scope.entity,$scope.code).success(function(response) {
			if(response.success){
				alert("注册成功！");
			} else {
				alert(response.message);
			}
		});
		
	}
	
	// 生成验证码
	$scope.genSmsCode = function() {
		userService.genSmsCode($scope.entity.phone).success(function(response){
			if(response.success){
				alert("短信发送成功！");
			} else{
				alert(response.message);
			}
		});
	}
	
});	
