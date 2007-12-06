/*
 * ====================================================================
 * Copyright (c) 2004-2007 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.io.fs;

import java.util.ArrayList;
import java.util.Arrays;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.ISVNLocationEntryHandler;
import org.tmatesoft.svn.core.io.ISVNLocationSegmentHandler;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.io.SVNRepository;


/**
 * @version 1.1.2
 * @author  TMate Software Ltd.
 */
public class FSLocationsFinder {
    
    private FSFS myFSFS;

    public FSLocationsFinder(FSFS fsfs) {
        myFSFS = fsfs;
    }
    
    public int traceNodeLocations(String path, long pegRevision, long[] revisions, 
            ISVNLocationEntryHandler handler) throws SVNException {
        ArrayList locationEntries = new ArrayList(0);
        long[] locationRevs = new long[revisions.length];
        long revision;
        Arrays.sort(revisions);

        for (int i = 0; i < revisions.length; ++i) {
            locationRevs[i] = revisions[revisions.length - (i + 1)];
        }

        int count = 0;
        boolean isAncestor = false;
        
        for (count = 0; count < locationRevs.length && locationRevs[count] > pegRevision; ++count) {
            isAncestor = FSNodeHistory.checkAncestryOfPegPath(path, pegRevision, locationRevs[count], myFSFS);
            if (isAncestor) {
                break;
            }
        }
        
        if (count >= locationRevs.length) {
            return 0;
        }
        revision = isAncestor ? locationRevs[count] : pegRevision;

        FSRevisionRoot root = null;
        while (count < revisions.length) {
            long[] appearedRevision = new long[1];
            SVNLocationEntry previousLocation = getPreviousLocation(path, revision, appearedRevision);
            if (previousLocation == null) {
                break;
            }
            String previousPath = previousLocation.getPath();
            if (previousPath == null) {
                break;
            }
            long previousRevision = previousLocation.getRevision();
            while ((count < revisions.length) && (locationRevs[count] >= appearedRevision[0])) {
                locationEntries.add(new SVNLocationEntry(locationRevs[count], path));
                ++count;
            }

            while ((count < revisions.length) && locationRevs[count] > previousRevision) {
                ++count;
            }
            
            path = previousPath;
            revision = previousRevision;
        }
        
        root = myFSFS.createRevisionRoot(revision);
        FSRevisionNode curNode = root.getRevisionNode(path);

        while (count < revisions.length) {
            root = myFSFS.createRevisionRoot(locationRevs[count]);
            if (root.checkNodeKind(path) == SVNNodeKind.NONE) {
                break;
            }
            FSRevisionNode currentNode = root.getRevisionNode(path);
            if (!curNode.getId().isRelated(currentNode.getId())) {
                break;
            }
            locationEntries.add(new SVNLocationEntry(locationRevs[count], path));
            ++count;
        }
        
        for (count = 0; count < locationEntries.size(); count++) {
            if (handler != null) {
                handler.handleLocationEntry((SVNLocationEntry) locationEntries.get(count));
            }
        }
        return count;
    }
    
    public void getNodeLocationSegments(String path, long pegRevision, long startRevision, 
            long endRevision, ISVNLocationSegmentHandler handler) throws SVNException {
        long youngestRevision = SVNRepository.INVALID_REVISION; 
        if (FSRepository.isInvalidRevision(pegRevision)) {
            pegRevision = youngestRevision = myFSFS.getYoungestRevision();
        }
        
        if (FSRepository.isInvalidRevision(startRevision)) {
            if (FSRepository.isValidRevision(youngestRevision)) {
                startRevision = youngestRevision;
            } else {
                startRevision = myFSFS.getYoungestRevision();
            }
        }
        
        if (FSRepository.isInvalidRevision(endRevision)) {
            endRevision = 0;
        }
        
        if (endRevision > startRevision) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, 
                    "End revision {0,number,integer} must be less or equal to start revision {1,number,integer}",
                    new Object[] { new Long(endRevision), new Long(startRevision) });
            SVNErrorManager.error(err);
        }

        if (pegRevision < startRevision) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, 
                    "Peg revision {0,number,integer} must be greater or equal to start revision {1,number,integer}",
                    new Object[] { new Long(pegRevision), new Long(startRevision) });
            SVNErrorManager.error(err);
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        long currentRevision = pegRevision;
        while (currentRevision >= endRevision) {
            long[] appearedRevision = new long[1];
            
        }
    }

    protected void reset(FSFS fsfs) {
        myFSFS = fsfs;
    }

    private SVNLocationEntry getPreviousLocation(String path, long revision, long[] appearedRevision) throws SVNException {
        appearedRevision[0] = SVNRepository.INVALID_REVISION;
        FSRevisionRoot root = myFSFS.createRevisionRoot(revision);
        FSClosestCopy closestCopy = root.getClosestCopy(path);
        if (closestCopy == null) {
            return null;
        }
        
        FSRevisionRoot copyTargetRoot = closestCopy.getRevisionRoot();
        if (copyTargetRoot == null) {
            return null;
        }
        String copyTargetPath = closestCopy.getPath();
        FSRevisionNode copyFromNode = copyTargetRoot.getRevisionNode(copyTargetPath);
        String copyFromPath = copyFromNode.getCopyFromPath();
        long copyFromRevision = copyFromNode.getCopyFromRevision();
        String remainder = "";
        if (!path.equals(copyTargetPath)) {
            remainder = path.substring(copyTargetPath.length());
            if (remainder.startsWith("/")) {
                remainder = remainder.substring(1);
            }
        }
        String previousPath = SVNPathUtil.getAbsolutePath(SVNPathUtil.append(copyFromPath, remainder));
        appearedRevision[0] = copyTargetRoot.getRevision();
        return new SVNLocationEntry(copyFromRevision, previousPath);
    }
}
