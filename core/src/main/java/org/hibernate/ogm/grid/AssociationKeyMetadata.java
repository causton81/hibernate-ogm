/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores metadata information common to all keys related
  * to a given association
 *
 * @author Emmanuel Bernard &lt;emmanuel@hibernate.org&gt;
 */
public class AssociationKeyMetadata {
	private final String table;
	private final String[] columnNames;
	private final int hashCode;

	// not part of the object identity
	private final String[] rowKeyColumnNames;

	public AssociationKeyMetadata(String table, String[] columnNames, String[] rowKeyColumnNames) {
		this.table = table;
		this.columnNames = columnNames;
		this.rowKeyColumnNames = rowKeyColumnNames;

		// table hashing should be specific enough
		this.hashCode = table.hashCode();
	}

	public String getTable() {
		return table;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public String[] getRowKeyColumnNames() {
		return rowKeyColumnNames;
	}

	/**
	 * Returns all those columns from the given candidate list which are not part of this key family.
	 * <p>
	 * Stores can opt to persist only the returned columns when writing a row of this key family. All other columns can
	 * be retrieved from the key meta-data itself when reading an association row.
	 */
	public String[] getColumnsWithoutKeyColumns(Iterable<String> candidates) {
		List<String> nonKeyColumns = new ArrayList<String>();
		for ( String column : candidates ) {
			// exclude columns from the associationKey as they can be guessed via metadata
			if ( !isKeyColumn( column ) ) {
				nonKeyColumns.add( column );
			}
		}

		return nonKeyColumns.toArray( new String[nonKeyColumns.size()] );
	}

	/**
	 * Returns the name of the single row key column which is not a column of this key itself, in case such a column
	 * exists.
	 * <p>
	 * If e.g. an association key contains the column "bankAccounts_id" and the row key columns are "bankAccounts_id"
	 * and "owners_id", this method will return "owners_id". But if the row columns were "bankAccounts_id", "owners_id"
	 * and "order", {@code null} would be returned as there were more than one column which are not part of the
	 * association key.
	 *
	 * @return the name of the single row key column which is not a column of this key itself or {@code null} if there
	 * is either no or more than one such column.
	 */
	public String getSingleRowKeyColumnNotContainedInAssociationKey() {
		String nonKeyColumn = null;

		for ( String column : getRowKeyColumnNames() ) {
			if ( !isKeyColumn( column ) ) {
				if ( nonKeyColumn != null ) {
					// more than one column which is not contained in the association key; thus rows of this
					// association will be represented by DBObjects rather than single String, int etc. objects
					return null;
				}
				else {
					nonKeyColumn = column;
				}
			}
		}

		return nonKeyColumn;
	}

	/**
	 * Whether the given column is part of this key family or not.
	 *
	 * @return {@code true} if the given column is part of this key, {@code false} otherwise.
	 */
	public boolean isKeyColumn(String columnName) {
		for ( String keyColumName : getColumnNames() ) {
			if ( keyColumName.equals( columnName ) ) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || AssociationKeyMetadata.class != o.getClass() ) {
			return false;
		}

		AssociationKeyMetadata that = (AssociationKeyMetadata) o;

		// order of comparison matters on performance:
		if ( !table.equals( that.table ) ) {
			return false;
		}

		if ( !Arrays.equals( columnNames, that.columnNames ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "AssociationKeyMetadata" );
		sb.append( "{table='" ).append( table ).append( '\'' );
		sb.append( ", columnNames=" ).append( columnNames == null ? "null" : Arrays.asList( columnNames ).toString() );
		sb.append( '}' );
		return sb.toString();
	}
}
