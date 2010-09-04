/*
 * ====================================================================
 * Copyright (c) 2004-2010 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.wc17.db.statement;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.db.SVNSqlJetDb;
import org.tmatesoft.svn.core.internal.db.SVNSqlJetSelectFieldsStatement;

/**
 * select presence, kind, checksum, translated_size, 
 *   changed_rev, changed_date, changed_author, depth, symlink_target,
 *   copyfrom_repos_id, copyfrom_repos_path, copyfrom_revnum,
 *   moved_here, moved_to, last_mod_time, properties from working_node
 * where wc_id = ?1 and local_relpath = ?2;
 * 
 * @author TMate Software Ltd.
 */
public class SVNWCDbSelectWorkingNode extends SVNSqlJetSelectFieldsStatement<SVNWCDbSchema.WORKING_NODE__Fields> {

    public SVNWCDbSelectWorkingNode(SVNSqlJetDb sDb) throws SVNException {
        super(sDb, SVNWCDbSchema.WORKING_NODE);
    }

    protected void defineFields() {
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.presence);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.kind);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.checksum);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.translated_size);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.changed_rev);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.changed_author);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.depth);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.symlink_target);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.copyfrom_repos_id);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.copyfrom_repos_path);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.copyfrom_revnum);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.moved_here);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.moved_to);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.last_mod_time);
        fields.add(SVNWCDbSchema.WORKING_NODE__Fields.properties);
    }

}