var fieldMenuVM = new Vue({
        el: '#userManage',
        data: function() {
            return {
                dataTable:[],
                pager:{
                    total: 0,
                    currentPage: 1,
                    pageSize: 20,
                },
                multipleSelection: [],
                dialogFormVisible: false,
                form: {
                	id:'',
                    menuCode:'',
                    menuName:'',
                    beanName:''
                },
                oldMenuCode:'',//旧 菜单吗
                oldMenuName:'',//旧 菜单名称
                formLabelWidth: '150px',
                inputMenuName:'',//菜单搜索框
                rules:{
                	menuCode:[
                		{ required: true, message: '菜单代码不能为空',trigger:'blur'},
                		{validator:function(rule,value,callback){
                			  var id = fieldMenuVM.form.id;
                              var name = fieldMenuVM.oldMenuCode;
                              if(id && value==name){
                                  callback();
                              }
                			
                		     var param = {'menuCode':value};
                             axios.post('/customfield/customField/isExistsFieldMenu',param)
                             .then(function (response) {
                                 var data =  response.data;
                                 if(data.code=='0'){
                                     var resData = data.data;
                                     if(resData){
                                         callback(new Error("此菜单代码已存在，请修改后提交"));
                                     }else{
                                         callback();
                                     }
                                     
                                 }else{
                                      callback(new Error("查询菜单代码是否唯一报错"));
                                 }
                             })
                             .catch(function (error) {
                               console.log(error);
                             })
                             .then(function () {
                               // always executed
                             });
                			
                		},trigger:'blur'}
                		],
                	menuName:[
                		  { required: true, message: '菜单名称不能为空',trigger:'blur'},
                		  {validator:function(rule,value,callback){
                			     var id = fieldMenuVM.form.id;
                                 var name = fieldMenuVM.oldMenuName;
                                 if(id && value==name){
                                     callback();
                                 }
                			    var param = {'menuName':value};
                                axios.post('/customfield/customField/isExistsFieldMenu',param)
                                .then(function (response) {
                                    var data =  response.data;
                                    if(data.code=='0'){
                                        var resData = data.data;
                                        if(resData){
                                            callback(new Error("此菜单菜单名称已存在，请修改后提交"));
                                        }else{
                                            callback();
                                        }
                                        
                                    }else{
                                         callback(new Error("查询菜单菜单名称是否唯一报错"));
                                    }
                                })
                                .catch(function (error) {
                                  console.log(error);
                                })
                                .then(function () {
                                  // always executed
                                });
                			  
                		  },trigger:'blur'}
                		
                		
                	]
                
                		
                	
                }//rules end
            }        	  
        },
        methods: {
        	cancelForm(formName) {
                this.$refs[formName].resetFields();
            	this.dialogFormVisible = false;
            },
            deleteFieldMenu() {
            	
            	   var rows = this.multipleSelection;
                   if(rows.length==0){
                       this.$message({
                          message: '请选择一条数据进行修改',
                          type: 'warning'
                        });
                       return;
                   }
                   var rowNames = [];
                   var rowIds = [];
                   for(var i=0;i<rows.length;i++){
                	   var curRow = rows[i];
                       rowIds.push(curRow.id);
                       rowNames.push("["+curRow.menuName+"]");
                   }
                   
                   
                   var menuName = rowNames.join(" ");
                this.$confirm('确定要删除 '+menuName+' 菜单吗？', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                	  //删除
                	var param  = {idList:rowIds};
                    axios.post('/customfield/customField/deleteMenu',param)
                    .then(function (response) {
                        var data =  response.data;
                        if(data.code=='0'){
                        	fieldMenuVM.$message({
                                type: 'info',
                                message: '删除成功'
                            });   
                            
                        }else{
                        	fieldMenuVM.$message({
                                message: "接口调用失败",
                                type: 'warning'
                              }); 
                        }
                    })
                    .catch(function (error) {
                      console.log(error);
                    })
                    .then(function () {
                    	fieldMenuVM.initCustomFiledMenu();
                    });
                	
                	
                   
                }).catch(() => {
                       
                });
            },
            saveFieldMenu(formName){//保存自定义字段
                this.$refs[formName].validate((valid) => {
                    if (valid) {
                       var param=this.form;
                      axios.post('/customfield/customField/saveOrUpdateMenu', param)
                      .then(function (response) {
                          var resData = response.data;
                          if(resData.code=='0'){
                              fieldMenuVM.$message('操作成功');
                              fieldMenuVM.cancelForm(formName);
                              fieldMenuVM.initCustomFiledMenu();
                          }else{
                              fieldMenuVM.$message('操作失败');
                              console.error(resData);
                          }
                      
                      })
                      .catch(function (error) {
                           console.log(error);
                      });
                       
                    } else {
                      return false;
                    }
                  });
            	

            },// method end
            handleSelectionChange(val) {
                this.multipleSelection = val;
            },
            modifyFiledMenu(){//点击修改菜单页面按钮
            	var rows = this.multipleSelection;
                if(rows.length!=1){
                    this.$message({
                       message: '请选择一条数据进行修改',
                       type: 'warning'
                     });
                    return;
                }
                
                
                var param={id:rows[0].id};
                //根据id获取数据
                axios.post('/customfield/customField/queryFieldMenuById',param)
                .then(function (response) {
                    var data =  response.data;
                    if(data.code=='0'){
                    	fieldMenuVM.form= data.data;
                        //把当前的值存在临时变量里，当修改时，旧值和新值对比
                        fieldMenuVM.oldMenuCode = data.data.menuCode;
                        fieldMenuVM.oldMenuName = data.data.menuName;
                    }else{
                    	console.error(data);
                    }
                    
                   
                })
                .catch(function (error) {
                  console.log(error);
                })
                .then(function () {
                  // always executed
                }); 
                
                
                this.dialogFormVisible = true;
            	
            	
            },//end
            initCustomFiledMenu(){//初始化table 表格
            	 var pageSize = this.pager.pageSize;
                 var pageNum = this.pager.currentPage;
                 var param = {};
                 param.menuName=this.inputMenuName;
                 axios.post('/customfield/customField/listMenuPage?pageNum='+pageNum+"&pageSize="+pageSize,param)
                     .then(function (response) {
                         var data =  response.data;
                         if(data.code=='0'){
                             var resData = data.data;
                             fieldMenuVM.dataTable= resData.data;
                             //3.分页组件
                             fieldMenuVM.pager.total= resData.total;
                             fieldMenuVM.pager.currentPage = resData.currentPage;
                             fieldMenuVM.pager.pageSize = resData.pageSize;
                         }else{
                        	 console.error(data);
                         }
                        
                     })
                     .catch(function (error) {
                       console.log(error);
                     })
                     .then(function () {
                       // always executed
                     }); 
            },
            goToFieldSettingPage(row){
            	console.info(row);
            	var id = row.id;
            	location.href= "/customfield/customField/customFieldPage?id="+id;
            },
            closeAddCustomFieldDialog(){//关闭添加自定义字段dialog
          	  this.$refs['customMenuForm'].resetFields();
            }
            
            
        },//methods end
        created(){
        	this.initCustomFiledMenu();
        }
      
    })