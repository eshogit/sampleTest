package sample4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class sample4 {
	static public void main(String args[]) {
		System.out.println("Q.何回以上連続してタイムアウトした場合に故障とさせますか？");
		System.out.println("A." + args[0] + "回");
		int ncount = Integer.parseInt(args[0]);

		String inputfile = "logfolder/logfile4.txt";
		String outputfile = "output4.txt";

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

		List<Duration> timeDurationOutput = new ArrayList<>();

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
				yearlist.add(Integer.parseInt(datacanma[0].substring(0, 4)));
				monthlist.add(Integer.parseInt(datacanma[0].substring(4, 6)));
				daylist.add(Integer.parseInt(datacanma[0].substring(6, 8)));
				hourlist.add(Integer.parseInt(datacanma[0].substring(8, 10)));
				minutelist.add(Integer.parseInt(datacanma[0].substring(10, 12)));
				secondlist.add(Integer.parseInt(datacanma[0].substring(12, 14)));

				// アドレス
				String[] addslash = datacanma[1].split("/");
				String[] addpiliod = addslash[0].split(Pattern.quote("."));
				int[] address = new int[addpiliod.length];

				// サブネットマスク
				int subnum = Integer.parseInt(addslash[1]);
				int[] subnet = new int[4];

				int k = 1;
				for (int i = 0; i <= 3; i++) {
					address[i] = Integer.parseInt(addpiliod[i]);
					//System.out.println(address[i]);
					for (int j = 7; j>=0; j--) {
						if (k <= subnum) {
							subnet[i] = subnet[i] + (int) Math.pow(2, j);
							k++;
						}
					}
					//System.out.println(subnet[i]);
				}

				int[] subadd = new int[4];
				subadd[0] = address[0] & subnet[0];
				subadd[1] = address[1] & subnet[1];
				subadd[2] = address[2] & subnet[2];
				subadd[3] = address[3] & subnet[3];

				String addStr = String.valueOf(subadd[0]) + "." + String.valueOf(subadd[1]) + "."
						+ String.valueOf(subadd[2]) + "." + String.valueOf(subadd[3]);
				//System.out.println(addStr);
				
				serverlist.add(addStr);
				timelist.add(datacanma[2]);
			}

			// 最後にファイルを閉じてリソースを開放する
			bufferedReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 故障期間の計算
		for (int i = 0; i < timelist.size(); i++) {

			String searchServer = serverlist.get(i);

			int check = -1;
			if (serverIndexNormal.size() == 0) {
				check = 1;
			} else {
				int k = 0;
				for (int t = 0; t < serverIndexNormal.size(); t++) {
					if (serverNameNormal.get(t).equals(searchServer) && i >= serverIndexNormal.get(t)) {
						check = 1;
						break;
					} else if (!serverNameNormal.get(t).equals(searchServer)) {
						k++;
					}
				}

				if (k == serverIndexNormal.size()) {
					check = 1;
				}
			}

			if (timelist.get(i).equals("-") && check == 1) {
				// 連続で「-」の回数
				int renzoku = 1;
				for (int j = i + 1; j < timelist.size(); j++) {
					if (timelist.get(j).equals("-") && serverlist.get(j).equals(searchServer)) {
						renzoku++;
					} 
					else if(renzoku < ncount){
						break;
					}
					
					if (!timelist.get(j).equals("-") && serverlist.get(j).equals(searchServer)
							&& renzoku >= ncount) {
						
						// タイムアウトした時間
						LocalDateTime begintime = LocalDateTime.of(yearlist.get(i), monthlist.get(i), daylist.get(i),
								hourlist.get(i), minutelist.get(i), secondlist.get(i));
						// pingが返ってきた時間
						LocalDateTime endtime = LocalDateTime.of(yearlist.get(j), monthlist.get(j), daylist.get(j),
								hourlist.get(j), minutelist.get(j), secondlist.get(j));
						// 故障期間
						Duration duration = Duration.between(begintime, endtime);
						
						// 合計
						// サーバーが出力リストに追加されているかのチェック
						int addindex = 0;
						int addcheck = -1;
						for (int t = 0; t < serverOutput.size(); t++) {
							if (serverOutput.get(t).equals(searchServer)) {
								addindex = t;
								addcheck = 0;
							}
						}

						if (addcheck == 0) {
							// 既存のに故障期間を足す
							Duration sum = duration.plus(timeDurationOutput.get(addindex));
							timeOutput.set(addindex, sum.toString());
							timeDurationOutput.set(addindex, sum);
						} else {
							// 新しく追加
							serverOutput.add(serverlist.get(j));
							timeOutput.add(duration.toString());
							timeDurationOutput.add(duration);
						}

						// 正常のサーバ
						if (serverIndexNormal.size() == 0) {
							serverIndexNormal.add(j);
							serverNameNormal.add(searchServer);
						} else {
							int k = -1;
							for (int t = 0; t < serverIndexNormal.size(); t++) {
								if (serverNameNormal.get(t).equals(searchServer)) {
									serverIndexNormal.set(t, j);
									k = 1;
									break;
								}
							}
							if (k == -1) {
								serverIndexNormal.add(j);
								serverNameNormal.add(searchServer);
							}
						}

						break;
					}
				}
			}
		}

		// ファイル出力
		try {
			FileWriter fw = new FileWriter(outputfile);
			fw.write("サブネットアドレス,故障期間");
			for (int i = 0; i < timeOutput.size(); i++) {
				fw.write("\n");
				fw.write(serverOutput.get(i) + "," + timeOutput.get(i));
			}
			fw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
