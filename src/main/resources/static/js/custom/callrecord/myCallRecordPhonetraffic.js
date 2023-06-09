var myCallRecordVm = new Vue({
    el: '#myCallRecordVm',
    data: {
        audioShow:false,
        isShow:false,
        formLabelWidth:'120px',
        pager:{//组织列表pager
            total: 0,
            currentPage: 1,
            pageSize: 20,
        },
        totalTalkTime:0,
        callRecordData:[],
        callStatus:[{
            value: "-1",
            label: '全部'
        }, {
            value: "1",
            label: '接通'
        }, {
            value: "0",
            label: '未接通'
        }],
        callTypeList:[{
            value: "-1",
            label: '全部'
        },{
            value: "1",
            label: '呼入'
        }, {
            value: "2",
            label: '外呼'
        }],
        searchForm:{
            callStatus:'',
            customerName:'',
            customerPhone:'',
            callType:'',
            startTime:'',
            endTime:''

        }
    },
    methods:{
        transCusPhone(row) {
            var text="";
            if( (roleCode =='DXCYGW' && (row.accountId !=undefined && row.accountId!=userId)) || (row.clueId != null &&( row.phase ==7 || row.phase == 8 || (roleCode =='DXCYGW' && (row.teleSaleId+"") != userId) ||  (roleCode =='DXZJ' && orgId !=(row.teleGorupId+""))))){
                text ="***"
            }else{
                text = row.customerPhone;
            }
            return text;
        },
        initCallRecordData(){
            var startTime = this.searchForm.startTime;
            var endTime = this.searchForm.endTime;
            var startTimestamp = Date.parse(new Date(startTime));
            if(endTime){
                var endTimestamp = new Date(endTime);
                if(startTimestamp> endTimestamp){
                    this.$message({
                        message: '开始时间不能大于结束时间',
                        type: 'warning'
                    });
                    return;
                }
            }
            var callStatus = this.searchForm.callStatus;
            if(callStatus=='-1'){
                this.searchForm.callStatus='';
            }
            var callType = this.searchForm.callType;
            if(callType=='-1'){
                this.searchForm.callType='';
            }
            var param = this.searchForm;
            param.pageNum=this.pager.currentPage;
            param.pageSize=this.pager.pageSize;
            axios.post('/trafficCall/callRecord/listMyCallRecord',param)
                .then(function (response) {
                    var data =  response.data
                    if(data.code=='0'){
                        var resData = data.data;
                        var callRecordData = resData.data;
                        var callRecordDataData = callRecordData.data;
                        for(var i=0;i<callRecordDataData.length;i++){
                            callRecordDataData[i].customerPhone=myCallRecordVm.transCusPhone(callRecordDataData[i]);
                        }
                        myCallRecordVm.callRecordData= callRecordData.data;
                        //3.分页组件
                        myCallRecordVm.pager.total= callRecordData.total;
                        myCallRecordVm.pager.currentPage = callRecordData.currentPage;
                        myCallRecordVm.pager.pageSize = callRecordData.pageSize;

                    }else{
                        myCallRecordVm.$message({message:data.msg,type:'error'});
                        console.error(data);
                    }

                })
                .catch(function (error) {
                    console.log(error);
                }).then(function(){
            });

            axios.post('/trafficCall/callRecord/countMyCallRecordTalkTime',param)
                .then(function (response) {
                    console.info(response);
                    var data =  response.data;
                    if(data.code=='0'){
                        myCallRecordVm.totalTalkTime = data.data;

                    }else{
                        myCallRecordVm.$message({message:data.msg,type:'error'});
                        console.error(data);
                    }

                })
                .catch(function (error) {
                    console.log(error);
                }).then(function(){
            });
        },
        getCallTypeText(row, column, value, index){
            var valText="";
            if(value=="1"){
                valText="呼入";
            }else if(value=="3"){
                valText="外呼";
            }

            return valText;
        }
        ,getCallStatusText(row, column, value, index){
            var valText="";
            if(value=="1" || value=="33" || value=="52"){
                valText="接通";
            }else{
                valText="未接通";
            }

            return valText;
        },
        getStartTimeText(row, column, value, index){
            var valText="";
            if(value){
                valText =  moment.unix(Number(value)).format("YYYY-MM-DD HH:mm:ss");
            }
            return valText;
        },
        getTalkTimeText(row, column, value, index){
            var valText="";
            if(value){
                valText =  this.formatSeconds(value);
            }
            return valText;
        },
        formatSeconds(s){
            var t="";
            var hour = Math.floor(s/3600);
            var min = Math.floor(s/60) % 60;
            var sec = s % 60;
            if(hour<10){
                t+="0";
            }
            t+=hour+":";
            if(min < 10){
                t += "0";
            }
            t += min + ":";
            if(sec < 10){
                t += "0";
            }
            t+=sec;
            // t += sec.toFixed(2);
            return t;
        },
        fomatSeconds2(s){
            var t="";
            var hour = Math.floor(s/3600);
            var min = Math.floor(s/60) % 60;
            var sec = s % 60;
            if(hour<10){
                t+="0";
            }
            t+=hour+"小时"
            if(min < 10){
                t += "0";
            }
            t += min + "分钟";
            if(sec < 10){
                t += "0";
            }
            t += sec + "秒";
            return t;
        },

        searchYesterday(){
            var today = new Date();
            today.setTime(today.getTime()-24*60*60*1000);
            var startTime = today.getFullYear()+"-" + (today.getMonth()+1) + "-" + today.getDate()+" 00:00:00";
            var endTime =today.getFullYear()+"-" + (today.getMonth()+1) + "-" + today.getDate()+" 23:59:59";
            this.searchForm.startTime=startTime;
            this.searchForm.endTime=endTime;
            this.initCallRecordData();
        },
        searchWeek(){
            var a = new Date();
            var year = a.getFullYear();
            var month = a.getMonth();
            var date = a.getDate();
            var b = new Date(year,month,date);
            var c = b.valueOf()-6*24*60*60*1000;
            // var d = b.valueOf()+1*24*60*60*1000;
            var d = b.valueOf();
            var startTime= new Date(c);
            var endTime = new Date(d);
            this.searchForm.startTime=startTime.getFullYear()+"-" + (startTime.getMonth()+1) + "-" + startTime.getDate()+" 00:00:00";
            this.searchForm.endTime=endTime.getFullYear()+"-" + (endTime.getMonth()+1) + "-" + endTime.getDate()+" 23:59:59";
            this.initCallRecordData();
        },
        searchMonth(){
            var a = new Date();
            var year = a.getFullYear();
            var month = a.getMonth();
            var date = a.getDate();
            var b = new Date(year,month,date);
            var f= new Date(year,month,01);
            var c = f.valueOf();
            //  var d = b.valueOf()+1*24*60*60*1000;
            var startTime= new Date(c);
            // var endTime = new Date(d);
            this.searchForm.startTime=startTime.getFullYear()+"-" + (startTime.getMonth()+1) + "-" + startTime.getDate()+" 00:00:00";;
            this.searchForm.endTime=year+"-"+(month+1)+"-"+date+" 23:59:59";
            this.initCallRecordData();
        },
        downloadAudio(id,url,callSource){
            var param = {};
            param.id=id;
            axios.post('/trafficCall/callRecord/getRecordFile',param)
                .then(function (response) {
                    var data =  response.data;
                    if(data.code=='0'){
                        var url = data.data;
                        if(url){
                            var fileName = url.split('?')[0];
                            var fileNameArr= fileName.split("/");
                            //download(url,fileNameArr[fileNameArr.length-1],'audio/*');

                            var x=new XMLHttpRequest();
                            x.open("GET", url, true);
                            x.responseType = 'blob';
                            x.onload=function(e){download(x.response, fileNameArr[fileNameArr.length-1], 'audio/*' ); }
                            x.send();
                        }

                    }else{
                        console.error(data);
                        myCallRecordVm.$message({message:data.msg,type:'error'});
                    }

                })
                .catch(function (error) {
                    console.log(error);
                }).then(function(){
            });

        },
        switchSoundBtn(id,url,callSource){
            // this.audioShow=true;
            if(callSource=='2' || callSource=='3'){
                // switchSound(id,url);
                var httpUrl = url.replace("https","http");
                window.parent.open(httpUrl)
                return;
            }
            if(callSource == '6'){
                var httpUrl = url.replace("https","http");
                var page = window.open();
                var html = "<body style='background: #000;text-align: center;position: relative'><div style='position: absolute;top: 50%;left: 50%;margin-left: -150px;margin-top:-30px;'><audio src='"+httpUrl+"' controls='controls' autoplay></audio></div></body>";
                page.document.write(html);
                return;
            }
            var newWindow = window.open();
            var param = {};
            param.id=id;
            axios.post('/trafficCall/callRecord/getRecordFile',param)
                .then(function (response) {
                    var data =  response.data
                    if(data.code=='0'){
                        var url = data.data;
                        // switchSound(id,url);
                        var httpUrl = url.replace("https","http");
                        newWindow.location.href = httpUrl;
                    }else{
                        console.error(data);
                        myCallRecordVm.$message({message:data.msg,type:'error'});
                    }

                })
                .catch(function (error) {
                    console.log(error);
                }).then(function(){
            });
        },
        toogleClick(){
            if(this.isShow){
                this.isShow=false
            }else{
                this.isShow=true
            }
        },
    },
    created(){
        var a = new Date();
        var year = a.getFullYear();
        var month = a.getMonth();
        var date = a.getDate();
        this.searchForm.startTime=year+"-" + (month+1) + "-" + date+" 00:00:00";
        this.searchForm.endTime=year+"-"+(month+1)+"-"+date+" 23:59:59";
        // 取页数存储
        var localVal=localStorage.getItem('allChangePageSize')?parseInt(localStorage.getItem('allChangePageSize')):'';
        if(localVal){this.pager.pageSize = localVal;}
        this.initCallRecordData();
    },
    mounted(){
        document.getElementById('myCallRecordVm').style.display = 'block';
    },
    filters:{
        fomatSeconds2(s){
            var t="";
            var hour = Math.floor(s/3600);
            var min = Math.floor(s/60) % 60;
            var sec = s % 60;
            if(hour<10){
                t+="0";
            }
            t+=hour+"小时"
            if(min < 10){
                t += "0";
            }
            t += min + "分钟";
            if(sec < 10){
                t += "0";
            }
            t += sec + "秒";
            return t;
        }
    }

})