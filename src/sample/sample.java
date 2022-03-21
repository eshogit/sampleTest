package sample;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.io.FileWriter;

public class sample {
	 static public void main(String args[]){
		 String inputfile = "logfolder/logfile.txt";
		 String outputfile = "output.txt";
		 
		 List<Integer> yearlist = new ArrayList<>();
		 List<Integer> monthlist = new ArrayList<>();
		 List<Integer> daylist = new ArrayList<>();
		 List<Integer> hourlist = new ArrayList<>();
		 List<Integer> minutelist = new ArrayList<>();
		 List<Integer> secondlist = new ArrayList<>();
		 List<String> serverlist = new ArrayList<>();
		 List<String> timelist = new ArrayList<>();
		 
		 List<String> serverOutput = new ArrayList<>();
		 List<String> timeOutput = new ArrayList<>();
		 
		 List<Integer> serverIndexNormal = new ArrayList<>();
		 List<String> serverNameNormal = new ArrayList<>();
		 
//		 List<Duration> timeDurationOutput = new ArrayList<>();
		 
		 try {
	            // ファイルのパスを指定する
	            File file = new File(inputfile);
	         
	            // ファイルが存在しない場合に例外が発生するので確認する
	            if (!file.exists()) {
	                System.out.print("ファイルが存在しません");
	                return;
	            }
	         
	            // BufferedReaderクラスのreadLineメソッドを使って1行ずつ読み込み表示する
	            FileReader fileReader = new FileReader(file);
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	            String data;
	            while ((data = bufferedReader.readLine()) != null) {
	            	String[] datacanma = data.split(",");
	            	yearlist.add(Integer.parseInt(datacanma[0].substring(0,4)));
	            	monthlist.add(Integer.parseInt(datacanma[0].substring(4,6)));
	            	daylist.add(Integer.parseInt(datacanma[0].substring(6,8)));
	            	hourlist.add(Integer.parseInt(datacanma[0].substring(8,10)));
	            	minutelist.add(Integer.parseInt(datacanma[0].substring(10,12)));
	            	secondlist.add(Integer.parseInt(datacanma[0].substring(12,14)));
	            	serverlist.add(datacanma[1]);
	            	timelist.add(datacanma[2]);
	            }
	         
	            // 最後にファイルを閉じてリソースを開放する
	            bufferedReader.close();
	         
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		    
		    //故障期間の計算
		    for(int i=0; i<timelist.size();i++) {
		    	
        		String searchServer = serverlist.get(i);
        		
        		int check = -1;
        		if(serverIndexNormal.size() == 0) {
					check = 1;
				}
        		else {
        			int k = 0;
        			for(int t=0; t<serverIndexNormal.size(); t++) {
        				if(serverNameNormal.get(t).equals(searchServer) && i >= serverIndexNormal.get(t)) {
        					check = 1;
        					break;
        				}
        				else if(!serverNameNormal.get(t).equals(searchServer)){
        					k++;
        				}
        			}
        			
        			if(k == serverIndexNormal.size()) {
        				check = 1;
        			}
        		}
        		
	        	if(timelist.get(i).equals("-") && check == 1) {
	        		
	        		for(int j=i+1;j<timelist.size();j++) {
	        			if(!timelist.get(j).equals("-") && serverlist.get(j).equals(searchServer)) {
	        				//タイムアウトした時間
	        				LocalDateTime begintime = LocalDateTime.of(yearlist.get(i), monthlist.get(i), daylist.get(i), hourlist.get(i), minutelist.get(i),secondlist.get(i));
	    	        		//pingが返ってきた時間
	        				LocalDateTime endtime = LocalDateTime.of(yearlist.get(j), monthlist.get(j), daylist.get(j), hourlist.get(j), minutelist.get(j),secondlist.get(j));
	    	        		//故障期間
	    	        		Duration duration = Duration.between(begintime, endtime); 
	        				
	    	        		serverOutput.add(serverlist.get(j));
        					timeOutput.add(duration.toString());
        					
        				
        					if (serverIndexNormal.size() == 0) {
        						serverIndexNormal.add(j);
        						serverNameNormal.add(searchServer);
        					}
        					else {
        						int k = -1;
        						for(int t=0; t<serverIndexNormal.size(); t++) {
    	        					if(serverNameNormal.get(t).equals(searchServer)) {
    	        						serverIndexNormal.set(t,j);
    	        						k = 1;
    	        						break;
    	        					}
    	        				}
        						if(k == -1) {
        							serverIndexNormal.add(j);
            						serverNameNormal.add(searchServer);
        						}
        					}
	        				
	        				break;
	        			}
	        		}	
	        	}
	        }
	        
	      //ファイル出力
	        try {
	            FileWriter fw = new FileWriter(outputfile);
	            fw.write("サーバー名,故障期間");
	            for(int i=0;i<timeOutput.size();i++) {
	            	fw.write("\n");
		            fw.write(serverOutput.get(i) + "," + timeOutput.get(i));
	            }
	            fw.close();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } 
	 }
}
