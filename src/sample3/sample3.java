package sample3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class sample3 {
	static public void main(String args[]) {
		System.out.println("Q.何回以上連続してタイムアウトした場合に故障とさせますか？");
		System.out.println("A." + args[0] + "回");
		System.out.println("直近" + args[0] + "回の平均応答時間が" + args[1] + "ミリ秒を超えた場合は、サーバが過負荷状態になっているとみなす");
		int ncount = Integer.parseInt(args[0]);
		int mcount = Integer.parseInt(args[0]);
		int ttime = Integer.parseInt(args[1]);

		String inputfile = "logfolder/logfile3.txt";
		String outputfile = "output3.txt";

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

		// 直近m回の平均応答時間
		List<String> hukaServer = new ArrayList<>();
		List<Integer> avetimedata = new ArrayList<>();
		List<Integer> countcheck = new ArrayList<>();
		List<LocalDateTime> hukabegin = new ArrayList<>();
		List<LocalDateTime> hukaend = new ArrayList<>();

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
				serverlist.add(datacanma[1]);
				timelist.add(datacanma[2]);
			}

			// 最後にファイルを閉じてリソースを開放する
			bufferedReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 平均応答時間
		for (int i = timelist.size() - 1; i >= 0; i--) {
			int k = -1;
			if (!timelist.get(i).equals("-")) {
				// System.out.println(hukaServer.size());
				for (int t = 0; t < hukaServer.size(); t++) {

					if (hukaServer.get(t).equals(serverlist.get(i))) {

						countcheck.set(t, countcheck.get(t) + 1);
						if (countcheck.get(t) < mcount) {

							avetimedata.set(t, (avetimedata.get(t) + Integer.parseInt(timelist.get(i))));
						} else if (countcheck.get(t) == mcount) {
							avetimedata.set(t, (avetimedata.get(t) + Integer.parseInt(timelist.get(i))));
							// m回目の日時
							LocalDateTime endtime = LocalDateTime.of(yearlist.get(i), monthlist.get(i), daylist.get(i),
									hourlist.get(i), minutelist.get(i), secondlist.get(i));
							hukaend.set(t, endtime);
						} else {
							countcheck.set(t, countcheck.get(t) - 1);
						}

						k = 1;
						break;
					}
				}

				if (k == -1) {
					hukaServer.add(serverlist.get(i));
					countcheck.add(1);
					avetimedata.add(Integer.parseInt(timelist.get(i)));
					// 1回目の日時
					LocalDateTime begintime = LocalDateTime.of(yearlist.get(i), monthlist.get(i), daylist.get(i),
							hourlist.get(i), minutelist.get(i), secondlist.get(i));
					hukabegin.add(begintime);

					hukaend.add(begintime); // 初期値
				}
			}
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
					} else if (!timelist.get(j).equals("-") && serverlist.get(j).equals(searchServer)
							&& renzoku >= ncount) {
						// タイムアウトした時間
						LocalDateTime begintime = LocalDateTime.of(yearlist.get(i), monthlist.get(i), daylist.get(i),
								hourlist.get(i), minutelist.get(i), secondlist.get(i));
						// pingが返ってきた時間
						LocalDateTime endtime = LocalDateTime.of(yearlist.get(j), monthlist.get(j), daylist.get(j),
								hourlist.get(j), minutelist.get(j), secondlist.get(j));
						// 故障期間
						Duration duration = Duration.between(begintime, endtime);

						serverOutput.add(serverlist.get(j));
						timeOutput.add(duration.toString());

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

			fw.write("サーバー名,過負荷状態期間");
			for (int i = 0; i < hukaServer.size(); i++) {
				if (countcheck.get(i) == mcount && (double) avetimedata.get(i) / mcount > ttime) {
					fw.write("\n");
					// 過負荷状態期間
					Duration hukaduration = Duration.between(hukaend.get(i), hukabegin.get(i));
					fw.write(hukaServer.get(i) + "," + hukaduration.toString());
				}
			}
			fw.write("\n");
			fw.write("\n");

			fw.write("サーバー名,故障期間");
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
