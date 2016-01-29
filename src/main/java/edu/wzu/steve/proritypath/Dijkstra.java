package edu.wzu.steve.proritypath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dijkstra {

	static void dijkstra(final Map<String, Station> stations,
			final List<Line> lines, final String start, final String end){
		if (!stations.containsKey(start)) {
			throw new IllegalArgumentException("start does not exist!");
		}
		for (Station s : stations.values()) { // �ڵ���Ϣ��ʼ��
			s.setDisSum(Constants.MAX_V);
			s.setPrevious(null);
		}
		stations.get(start).setDisSum(0);
		Map<String, Station> s = new HashMap<String, Station>();
		Map<String, Station> q = new HashMap<String, Station>();
		q.putAll(stations);

//		System.out.println("------s = " + s);

//		System.out.println("------q = " + q);

//		int i = 0;

		while (!q.isEmpty()) {
			Station u = Collections.min(q.values(), new StationDisComparator());

			// System.out.println("------u = " + u + "------"+i);

//			System.out.println("------u.getname = " + u.getName() + "---1");

//			System.out.println("------q.getname = " + q.values() + "---1");

			q.remove(u.getName());

//			System.out.println("------u.getname = " + u.getName() + "---2");

//			System.out.println("------q.getname = " + q.values() + "---2");

			s.put(u.getName(), u);

//			System.out.println("------u.getname = " + u.getName() + "---3");

//			System.out.println("------q.getname = " + q.values() + "---3");

//			i++;// �������

			if (u.getName().equals(end)) {

				System.out.println("u.getname = " + u.getName().equals(end));

				break;

			}

			int j = 0;

			for (Line l : lines) {



				j++;// �������

//				System.out.println("������������ j = " + j + "������������");

				if (l.getStart().equals(u.getName())) { // �ж������Ƿ�����u.getName������ʼ

//					System.out.println("l.getStart = " + l.getStart());

					Station v = stations.get(l.getEnd());

//					System.out.println("****** before v = " + v + "******" + i

//							+ "------" + j);

					if (v.getDisSum() > u.getDisSum() + l.getDis()) {

//						System.out.println("v.etDisSum = " + v.getDisSum()

//								+ "---u.getDisSum() = " + u.getDisSum()

//								+ "---l.getDis() = " + l.getDis());

						v.setDisSum(u.getDisSum() + l.getDis());

						v.setPrevious(u);

					}

//					System.out.println("****** after v = " + v + "******" + i

//							+ "------" + j);

				}

			}

		}

	}



	static void dijkstra(final Map<String, Station> stations,

			final List<Line> lines, final String start) {

		dijkstra(stations, lines, start, null);

	}



	static void showResult(final Map<String, Station> stations,

			final String end, boolean isPrintRoot) {



		if (!stations.containsKey(end)) {

			throw new IllegalArgumentException("end does not exist!");

		}



		Station s = stations.get(end);

//		System.out.println("-----------******------------");

//		System.out.println("Result:" + s);



		if (isPrintRoot) {



			List<Station> root = new ArrayList<Station>();

			do {

				root.add(0, s);

				s = s.getPrevious();

			} while (s != null);



			System.out.println(root);

		}

	}



	public static void main(String[] args) {



		Map<String, Station> stations = new HashMap<String, Station>();

		stations.put("A", new Station("A"));

		stations.put("B", new Station("B"));

		stations.put("C", new Station("C"));

		stations.put("D", new Station("D"));

		stations.put("E", new Station("E"));

		stations.put("F", new Station("F"));

		stations.put("G", new Station("G"));

		stations.put("H", new Station("H"));



		System.out.println(">>>>><<<<<<<===" + stations.values());

		for (Station s : stations.values()) {

			System.out.println(s);

		}

		List<Line> lines = new ArrayList<Line>();

		lines.add(new Line("A", "B", 20));

		lines.add(new Line("A", "D", 80));

		lines.add(new Line("A", "G", 90));

		lines.add(new Line("B", "F", 10));

		lines.add(new Line("C", "F", 50));

		lines.add(new Line("C", "H", 20));

		lines.add(new Line("C", "D", 10));

		lines.add(new Line("D", "C", 10));

		lines.add(new Line("D", "G", 20));

		lines.add(new Line("E", "G", 30));

		lines.add(new Line("E", "B", 50));

		lines.add(new Line("F", "C", 10));

		lines.add(new Line("F", "D", 40));

		lines.add(new Line("G", "A", 20));

		// System.out.println("------" + stations);

		dijkstra(stations, lines, "A");

		// System.out.println("******" + stations);

		boolean isPrintRoot = true;

		showResult(stations, "A", isPrintRoot);

		//

		// showResult(stations, "B", isPrintRoot);

		//

		// showResult(stations, "C", isPrintRoot);

		//

		// showResult(stations, "D", isPrintRoot);

		// showResult(stations, "E", isPrintRoot);

		// showResult(stations, "F", isPrintRoot);

		// showResult(stations, "G", isPrintRoot);

		// showResult(stations, "H", isPrintRoot);



	}

}

