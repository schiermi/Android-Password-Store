/*
 * Copyright © 2014-2021 The Android Password Store Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-only
 */
package dev.msfjarvis.aps.util.git.operation

import dev.msfjarvis.aps.util.git.sshj.ContinuationContainerActivity
import org.eclipse.jgit.api.ResetCommand

class ResetToRemoteOperation(callingActivity: ContinuationContainerActivity) : GitOperation(callingActivity) {

  override val commands =
    arrayOf(
      // Stage all files
      git.add().addFilepattern("."),
      // Fetch everything from the origin remote
      git.fetch().setRemote("origin"),
      // Do a hard reset to the remote branch. Equivalent to git reset --hard
      // origin/$remoteBranch
      git.reset().setRef("origin/$remoteBranch").setMode(ResetCommand.ResetType.HARD),
      // Force-create $remoteBranch if it doesn't exist. This covers the case where you
      // switched
      // branches from 'master' to anything else.
      git.branchCreate().setName(remoteBranch).setForce(true),
    )
}
