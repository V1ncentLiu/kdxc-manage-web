var mainDivVM = new Vue({
    el: '#mainDiv',
    data: {
        // 工作台
        activeName:'1',
        activeName2:'1',
        activeName3:'1',
        activeName4:'1',
        activeName5:'1',
        //公告        
        items: [ 
            // {content:'系统将于2018年12月5日晚上12:00进行系统升级，请各位同事及时处理工作。系统预计在12:20分恢复正常使用,感谢配合!',id:1},
            // {content:'公告2公告2公告2公告2公告2公告2公告2',id:2},
            // {content:'公告3公告3公告3公告3公告3公告3公告3',id:3}
        ], 
        //未读消息             
        consoleNewsParam:{
            newsBox:false,
            newsData: [
                // {content:'系统将于2018年12月5日晚上12:00进行系统升级，请各位同事及时处理工作。系统预计在12:20分恢复正常使用,感谢配合!',id:1},
                // {content:'公告2公告2公告2公告2公告2公告2公告2',id:2},
                // {content:'公告3公告3公告3公告3公告3公告3公告3',id:3}
            ]
        }
    },
    methods: {
        // 工作台
        handleClick(tab, event) {
            console.log(tab, event);
        },
        initBoard(){
            var param={};
            // 公告
            param={};
            axios.post('/console/console/queryAnnReceiveNoPage',param).then(function (response) {
                console.log(response.data)
                mainDivVM.items=response.data.data;
            });
            // 未读消息
            param={};
            axios.post('/console/console/queryBussReceiveNoPage',param).then(function (response) {
                console.log('未读消息')
                console.log(response.data)
                if(response.data){
                    if (response.data.data) {
                        if(response.data.data.length!=0){
                            mainDivVM.consoleNewsParam.newsData=response.data.data;
                            mainDivVM.consoleNewsParam.newsBox=true
                        }else{
                            mainDivVM.consoleNewsParam.newsBox=false
                        }
                    }else{
                        mainDivVM.consoleNewsParam.newsBox=false
                    }
                }else{
                    mainDivVM.consoleNewsParam.newsBox=false
                }                
            }); 
            // 待分配资源数
            // param={};
            // axios.post('/console/console/countTeleDircortoerUnAssignClueNum',param).then(function (response) {
            //     console.log(response.data)                
            //     mainDivVM.assignClueNum=response.data.data;
            // });
            // // 今日接受资源
            // param={};
            // axios.post('/console/console/countTeleDircortoerReceiveClueNum',param).then(function (response) {
            //     console.log(response.data)
            //     mainDivVM.receiveClueNum=response.data.data;
            // });
            // // 今日领取资源数
            // param={};
            // axios.post('/console/console/countTeleDircortoerGetClueNum',param).then(function (response) {
            //     console.log(response.data)
            //     mainDivVM.todaygetClueNum=response.data.data;
            // });
            // // 今日邀约数
            // param={};
            // axios.post('/console/console/countTeleDirectorTodayAppiontmentNum',param).then(function (response) {
            //     console.log(response.data)
            //     mainDivVM.todayAppiontmentNum=response.data.data;
            // });
            // // 预计明日到访数
            // param={};
            // axios.post('/console/console/countTeleDirecotorTomorrowArriveTime',param).then(function (response) {
            //     console.log(response.data)
            //     mainDivVM.tomorrowArriveTime=response.data.data;
            // });       
        },
    },
    created(){
        // 工作台
        this.initBoard();
    },
    mounted(){
        document.getElementById('mainDiv').style.display = 'block';
    }
});