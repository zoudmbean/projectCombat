app.service("loginService",function($http){
	this.getLoginname = function() {
		return $http.get("../login/getLoginName.do");
	}
});