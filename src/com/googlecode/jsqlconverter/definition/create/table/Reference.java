package com.googlecode.jsqlconverter.definition.create.table;

public class Reference {
	public enum Event { UPDATE, DELETE }
	public enum Action { RESTRICT, CASCADE, SET_NULL, NO_ACTION, SET_DEFAULT }
	public enum Match { FULL, PARTIAL, SIMPLE }

	public class ReferenceConstraint {
		private Event event;
		private Action action;

		public ReferenceConstraint(Event event, Action action) {
			this.event = event;
			this.action = action;
		}

		public Event getEvent() {
			return event;
		}

		public Action getAction() {
			return action;
		}
	}


	private String tableName;
	//private Vector<ReferenceConstraint> columnConstraints = new Vector<ReferenceConstraint>(); // can include size, and order
	private Match match;
	private Action updateAction;
	private Action deleteAction;

	public Reference(String tableName) {
		this.tableName = tableName;
	}

	// getters
	public String getTableName() {
		return tableName;
	}

	/*public Vector<ReferenceConstraint> getConstraints() {
		return columnConstraints;
	}*/

	public Action getUpdateAction() {
		return updateAction;
	}

	public Action getDeleteAction() {
		return deleteAction;
	}

	public Match getMatch() {
		return match;
	}

	// setters
	public void setMatch(Match match) {
		this.match = match;
	}

	public void setUpdate(Action action) {
		updateAction = action;
	}

	public void setDelete(Action delete) {
		deleteAction = delete;
	}

	// adders
	/*public void addConstraint(Event event, Action action) {
		columnConstraints.add(new ReferenceConstraint(event, action));
	}*/
}
