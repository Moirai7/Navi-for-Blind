package com.navi.util;

import java.util.*;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.navi.blind.BaseActivity;
import com.navi.client.Config;
import com.navi.model.Path;

public class PathOperationService extends Service {
	Database db;

	// for all
	Set<String> nodeID = new HashSet<String>();
	Set<String> roadID = new HashSet<String>();
	Map<String, Node> nodes = new HashMap<String, Node>();
	Map<String, Road> roads = new HashMap<String, Road>();
	Map<String, Path> allPoints = new HashMap<String, Path>();
	double aboutLen = 1.0;
	// for algorithm
	Map<String, Double> shortLenMap = new HashMap<String, Double>();
	Set<Node> inPath = new HashSet<Node>();
	Set<Node> outPath = new HashSet<Node>();
	Map<String, Double> aboutStartLenMap = new HashMap<String, Double>();
	Map<String, Double> aboutEndLenMap = new HashMap<String, Double>();
	Map<String, String> aboutEndMap = new HashMap<String, String>();
	List<String> endNodes = new ArrayList<String>();
	// results
	Map<String, String> path = new HashMap<String, String>();
	double minLen;
	String finalEndNode;
	String finalStartNode;
	List<String> shortestNodes = new ArrayList<String>();

	public void downloadInit() {
		db = Database.getInstance(this);
		db.setPlace();
		db.setRoads();
	}

	// run when sys start
	public void init() {
		db = Database.getInstance(this);
		nodeID = db.getNodes();
		for (String NodeID : nodeID) {
			Node node = new Node();
			node.nid = NodeID;
			Map<String, Double> child = db.getChild(NodeID);
			node.child = child;
			nodes.put(NodeID, node);
		}
		roadID = db.getRoads();
		for (String RoadID : roadID) {
			Road road = new Road();
			road.rid = RoadID;
			List<String> info = db.getRoad(RoadID);
			road.start = info.get(0);
			road.second = info.get(1);
			road.third = info.get(2);
			road.end = info.get(3);
			road.weight = Double.valueOf(info.get(4));
			roads.put(RoadID, road);
		}
		List<Path> All = db.readPaths();
		for (int i = 0; i < All.size(); i++) {
			if (!allPoints.containsKey(All.get(i).getPointID())) {
				allPoints.put(All.get(i).getPointID(), All.get(i));
			}
		}
	}

	// inner_class getstartId
	public void setStart(String NodeID, double Len) {
		shortLenMap.clear();
		inPath.clear();
		outPath.clear();
		Node start = new Node();
		start = nodes.get(NodeID);
		inPath.add(start);
		Iterator it = nodes.keySet().iterator();
		while (it.hasNext()) {
			String tmpNodeID = it.next().toString();
			if (!tmpNodeID.equals(NodeID)) {
				Node outer = new Node();
				outer.nid = tmpNodeID;
				outer = nodes.get(outer.nid);
				outPath.add(outer);
				shortLenMap.put(outer.nid, 9999999.0);
			}
		}
		shortLenMap.put(NodeID, Len);
	}

	// (!)all path
	public void getFindPath(String startNode, Node endNode, double len) {
		Map<String, String> tmpPath = new HashMap<String, String>();
		//
		// if (!nodeID.contains(startNode)&&!nodeID.contains(endNode.nid)){
		// Path tmpStartPath = allPoints.get(startNode);
		// Path tmpEndPath = allPoints.get(endNode.nid);
		// if (tmpStartPath.getStreetID().equals(tmpEndPath.getStreetID())){
		// minLen =
		// Math.abs(Double.valueOf(startNode)-Double.valueOf(endNode.nid));
		// path = tmpPath;
		// finalEndNode = endNode.nid;
		// finalStartNode = startNode;
		// return ;
		// }
		// }
		while (!inPath.contains(endNode)) {
			double mmin = 9999999.0;// max
			Node fromnode = new Node();
			Node nextnode = new Node();
			for (Node nownode : inPath) {
				for (String nxtNode : nownode.child.keySet()) {
					Node nxtnode = nodes.get(nxtNode);
					if (!inPath.contains(nxtnode)) {
						double tmpLen = shortLenMap.get(nownode.nid)
								+ nownode.child.get(nxtNode);
						if (mmin > tmpLen) {
							fromnode = nownode;
							nextnode = nodes.get(nxtNode);
							mmin = tmpLen;
						}
						if (shortLenMap.get(nxtNode) > tmpLen) {
							shortLenMap.remove(nxtNode);
							shortLenMap.put(nxtNode, tmpLen);
						}
					}
				}
			}
			tmpPath.put(nextnode.nid, fromnode.nid);
			inPath.add(nextnode);
			outPath.remove(nextnode);
		}
		if (minLen > (shortLenMap.get(endNode.nid) + len)) {
			minLen = shortLenMap.get(endNode.nid) + len;
			path = tmpPath;
			finalEndNode = endNode.nid;
			finalStartNode = startNode;
		}
		return;
	}

	// 获取下一路口方向
	protected String getDirection(int curIndex) {
		String speakOrder = "";
		Path a, b, c;
		a = allPoints.get(shortestNodes.get(curIndex - 1));
		b = allPoints.get(shortestNodes.get(curIndex));
		c = allPoints.get(shortestNodes.get(curIndex + 1));
		double x0, x1, x2, y0, y1, y2;
		x0 = Double.valueOf(a.getPointLatitude());
		y0 = Double.valueOf(a.getPointLongitude());
		x1 = Double.valueOf(b.getPointLatitude());
		y1 = Double.valueOf(b.getPointLongitude());
		x2 = Double.valueOf(c.getPointLatitude());
		y2 = Double.valueOf(c.getPointLongitude());
		// 计算左右
		if ((y1 - y0) / (x1 - x0) * (x2 - x0) + y0 > y2) {
			speakOrder = speakOrder + "右";
		} else {
			speakOrder = speakOrder + "左";
		}
		double v0x, v0y, v1x, v1y;
		v0x = x1 - x0;
		v0y = y1 - y0;
		v1x = x2 - x1;
		v1y = y2 - y1;
		// 计算转角（前后）
		double arc = (v0x * v1x - v0y * v1y)
				/ (Math.sqrt(v0x * v0x + v0y * v0y) * Math.sqrt(v1x * v1x + v1y
						* v1y));
		if (arc == 90) {
			speakOrder = speakOrder + "";
		} else if (arc > 90) {
			speakOrder = speakOrder + "后";
		} else {
			speakOrder = speakOrder + "前";
		}
		return speakOrder;
	}

	// find start things
	public void getAboutStartLen(String curNodeID) {
		aboutStartLenMap.clear();
		if (nodeID.contains(curNodeID)) {
			aboutStartLenMap.put(curNodeID, 0.0);
			return;
		} else {
			Path tmpPath = allPoints.get(curNodeID);
			// String aaa = tmpPath.getStreetID();
			Road tmpRoad = roads.get(tmpPath.getStreetID());
			aboutStartLenMap.put(
					tmpRoad.start,
					aboutLen
							+ aboutLen
							* Math.abs(Integer.valueOf(curNodeID)
									- Integer.valueOf(tmpRoad.second)));
			aboutStartLenMap.put(
					tmpRoad.end,
					aboutLen
							+ aboutLen
							* Math.abs(Integer.valueOf(curNodeID)
									- Integer.valueOf(tmpRoad.third)));
		}
	}

	// find end things
	public void getAboutEndLen(String place) {
		endNodes = db.getCertainNode(place);
		aboutEndLenMap.clear();
		if (endNodes.isEmpty())
			return;
		for (int i = 0; i < endNodes.size(); i++) {
			String tmpNode = endNodes.get(i);
			if (nodeID.contains(tmpNode)) {
				setAboutEndLenMap(tmpNode, tmpNode, 0.0);
			} else {
				Path tmpPath = allPoints.get(tmpNode);
				Road tmpRoad = roads.get(tmpPath.getStreetID());
				setAboutEndLenMap(
						tmpRoad.start,
						tmpNode,
						aboutLen
								+ aboutLen
								* Math.abs(Integer.valueOf(tmpNode)
										- Integer.valueOf(tmpRoad.second)));
				setAboutEndLenMap(
						tmpRoad.end,
						tmpNode,
						aboutLen
								+ aboutLen
								* Math.abs(Integer.valueOf(tmpNode)
										- Integer.valueOf(tmpRoad.third)));

			}
		}
	}

	// inner_class opt
	protected void setAboutEndLenMap(String node, String endnode, double len) {
		if (aboutEndLenMap.containsKey(node)) {
			if (aboutEndLenMap.get(node) > len) {
				aboutEndLenMap.remove(node);
				aboutEndLenMap.put(node, 0.0);
				aboutEndMap.remove(node);
				aboutEndMap.put(node, endnode);
			}
		} else {
			aboutEndLenMap.put(node, len);
			aboutEndMap.put(node, endnode);
		}
	}

	// inner_class checkpoint
	protected int checkCurPoint(String curNodeID) {
		int pos = shortestNodes.indexOf(curNodeID);
		if (pos == shortestNodes.size() - 1) {
			return 3;
		}
		if (pos == -1) {
			if (nodes.containsKey(curNodeID))
				return 0;
			Path tmpPath = allPoints.get(curNodeID);
			Road tmpRoad = roads.get(tmpPath.getStreetID());
			if (shortestNodes.indexOf(tmpRoad.start) == -1
					|| shortestNodes.indexOf(tmpRoad.start) == -1) {
				return 0;
			} else if (Math.abs(shortestNodes.indexOf(tmpRoad.start)
					- shortestNodes.indexOf(tmpRoad.end)) == 1) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 2;
		}
	}

	public class MyBinder extends Binder {

		public void initStartService() {
			init();
		}

		public void downloadInitService() {
			downloadInit();
		}

		public void findPath(String startID, String endName) {
			// 查找路径，返回下一步String
			shortestNodes.clear();
			getAboutStartLen(startID);
			getAboutEndLen(endName);
			if (aboutEndLenMap.isEmpty()) {
				String str = "没有这个地方呢";
				Message msg = Message.obtain();
				msg.what = Config.ACK_FINDPATH_FAIL;
				msg.obj = str;
				BaseActivity.sendMessage(msg);
				return;
			}
			minLen = 9999999.0;
			for (String startnode : aboutStartLenMap.keySet()) {
				for (String endnode : aboutEndLenMap.keySet()) {
					setStart(startnode, aboutStartLenMap.get(startnode));
					getFindPath(startnode, nodes.get(endnode),
							aboutEndLenMap.get(endnode));
				}
			}
			String tmpNode = finalEndNode;
			shortestNodes.add(tmpNode);
			if (!path.isEmpty())
				while (!path.get(tmpNode).equals(finalStartNode)) {
					shortestNodes.add(0, path.get(tmpNode));
					tmpNode = path.get(tmpNode);
				}
			if (!shortestNodes.get(0).equals(finalStartNode))
				shortestNodes.add(0, finalStartNode);
			if (!shortestNodes.get(0).equals(startID))
				shortestNodes.add(0, startID);
			shortestNodes.add(aboutEndMap.get(finalEndNode));

			String str = "选路成功，请向" + shortestNodes.get(1) + "走";
			Message msg = Message.obtain();
			msg.what = Config.ACK_FINDPATH_SUCCESS;
			msg.obj = str;
			BaseActivity.sendMessage(msg);
			return;
		}

		public void CheckPoint(String currentID) {
			// 判断是否偏离路径，返回下一步String
			String str = null;
			int result = checkCurPoint(currentID);
			if (result == 3) {
				str = "到了";
			} else if (result == 1) {
				str = "正确,继续前进";
			} else if (result == 2) {
				int pos = shortestNodes.indexOf(currentID);
				String speakOrder = getDirection(pos); // 寻找方向
				str = "正确，请向" + speakOrder + shortestNodes.get(pos + 1) + "走";
			} else {
				str = "偏离";
			}// TODO 问李为什么这里要多次使用checkCurPoint(currentID);
				// TODO 问李ACK_CHECKPOINT_FAIL是用来干什么的
			Message msg = Message.obtain();
			if (result == 0)
				msg.what = Config.FAIl;
			else if (result == 3) {
				msg.what = Config.ACK_END_POINT;// Config.ACK_END_POINT
			} else {
				msg.what = Config.ACK_CHECKPOINT_FAIL;// Config.FAIL
			}
			msg.obj = str;
			BaseActivity.sendMessage(msg);
		}

	}

	private MyBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	// struct
	public class Road {
		public String rid, start, second, third, end;
		public double weight;
	}

	public class Node {
		public String nid;
		public Map<String, Double> child = new HashMap<String, Double>();
	}

}
