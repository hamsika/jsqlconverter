package com.googlecode.jsqlconverter.definition.create.table;

import com.googlecode.jsqlconverter.definition.Name;

public class Reference {
	// TODO: support defferable

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

	private Name tableName;
	private Name columnName;

	private Match match;
	private Action updateAction;
	private Action deleteAction;

	public Reference(Name tableName, Name columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}

	// getters
	public Name getTableName() {
		return tableName;
	}

	public Name getColumnName() {
		return columnName;
	}

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
}
