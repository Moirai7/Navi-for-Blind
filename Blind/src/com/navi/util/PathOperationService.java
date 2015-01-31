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
	Database db = Database.getInstance();

	// for all
	Set<String> nodeID = new HashSet<String>();
	Set<String> roadID = new HashSet<String>();
	Map<String, Node> nodes = new HashMap<String, Node>();
	Map<String, Road> roads = new HashMap<String, Road>();
	Map<String, Path> allPoints = new HashMap<String, Path>();
	double aboutLen;
	// for algorithm
	Map<String, Double> shortLenMap = new HashMap<String, Double>();
	Set<Node> inPath = new HashSet<Node>();
	Set<Node> outPath = new HashSet<Node>();
	Map<String, Double> aboutStartLenMap = new HashMap<String, Double>();
	Map<String, Double> aboutEndLenMap = new HashMap<String, Double>();
	Map<String, String> aboutEndMap = new HashMap<String, String>();//建筑对应点
	List<String> endNodes = new ArrayList<String>();
	// results
	Map<String, String> path = new HashMap<String, String>();
	double minLen;
	String finalEndNode;
	String finalStartNode;
	List<String> shortestNodes = new ArrayList<String>();

	// inner_class getstartId
	public void setStart(String NodeID, double Len) {
		shortLenMap.clear();
		Node start = new Node();
		start = nodes.get(NodeID);
		inPath.add(start);
		Iterator it = nodes.keySet().iterator();
		while (it.hasNext()) {
			if (!it.next().toString().equals(NodeID)) {
				Node outer = new Node();
				outer.nid = it.next().toString();
				outer = nodes.get(outer.nid);
				outPath.add(outer);
				shortLenMap.put(outer.nid, 9999999.0);
			}
		}
		shortLenMap.put(NodeID, Len);
	}

	// run when sys start
	public void init() {
		db.setRoads();
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

	// (!)all path
	public void FindPath(String startNode, Node endNode, double len) {
		Map<String, String> tmpPath = new HashMap<String, String>();
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

	// find start things
	public void getAboutStartLen(String curNodeID) {
		aboutStartLenMap.clear();
		if (nodeID.contains(curNodeID)) {
			aboutStartLenMap.put(curNodeID, 0.0);
			return;
		} else {
			Path tmpPath = allPoints.get(curNodeID);
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

	//inner_class checkpoint
	protected int checkCurPoint(String curNodeID) {
		int pos = shortestNodes.indexOf(curNodeID);
		if (pos == shortestNodes.size()-1){
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
					- shortestNodes.indexOf(tmpRoad.start)) == 1) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 2;
		}
	}

	public class MyBinder extends Binder {
		
		public void initStartService(){
			init();
		}
		
		public void naviInfo(String startID,String baiduStr) {
			//int baiduStr.lastIndexOf("步行至");
			String endName=null;
			findPath(startID,endName);
		}
		
		public void findPath(String startID, String endName) {
			
			// 查找路径，返回下一步String
			getAboutStartLen(startID);
			getAboutEndLen(endName);
			minLen = 9999999.0;
			for (String startnode : aboutStartLenMap.keySet()) {
				setStart(startnode, aboutStartLenMap.get(startnode));
				for (String endnode : aboutEndLenMap.keySet()) {
					FindPath(startnode, nodes.get(endnode),
							aboutEndLenMap.get(endnode));
				}
			}
			String tmpNode = finalEndNode;
			shortestNodes.add(tmpNode);
			while (!path.get(tmpNode).equals(finalStartNode)) {
				shortestNodes.add(0, path.get(tmpNode));
				tmpNode = path.get(tmpNode);
			}
			shortestNodes.add(0, finalStartNode);
			shortestNodes.add(0, startID);
			shortestNodes.add(aboutEndMap.get(finalEndNode));
			
			String str = "选路成功，请向"+shortestNodes.get(1)+"走";
			Message msg = Message.obtain();
			msg.what = Config.SUCCESS;
			msg.obj = str;
			BaseActivity.sendMessage(msg);
		}

		public void CheckPoint(String currentID) {
			// 判断是否偏离路径，返回下一步String
			String str = null;
			if (checkCurPoint(currentID)==3){
				
			} else if (checkCurPoint(currentID)==1){
				str="正确";
			} else if (checkCurPoint(currentID)==2){
				int pos = shortestNodes.indexOf(currentID);
				str="正确，请向"+shortestNodes.get(pos+1)+"走";
			} else{
				str="偏离";
			}
			Message msg = Message.obtain();
			if (checkCurPoint(currentID)==0) msg.what = Config.FAIl;
			else if (checkCurPoint(currentID)==3){
				msg.what = Config.ACK_END_POINT;// Config.FAIL
			} else {
				msg.what = Config.FAIl;// Config.FAIL				
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
