package edu.nchu.cs.ai.solution;

import java.util.List;

import edu.nchu.cs.ai.bean.Path;

public class PathSolution extends Solution<List<Path>>{


	public PathSolution(List<Path> answer) {
		super(answer);
	}


	@Override
	public String toString() {
		List<Path> paths = this.getAns();
		StringBuilder sb = new StringBuilder();
		int cnt = 0;
		for (Path path:paths) {
			if (cnt == 0) {
				sb.append(path.getFrom().getName());
			}
			sb.append("\n").append(path.getTo().getName());
			cnt++;
		}
		return sb.toString();
	}

}
