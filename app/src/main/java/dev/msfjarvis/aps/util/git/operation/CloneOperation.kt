/*
 * Copyright © 2014-2021 The Android Password Store Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-only
 */
package dev.msfjarvis.aps.util.git.operation

import dev.msfjarvis.aps.util.git.sshj.ContinuationContainerActivity
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.GitCommand

/**
 * Creates a new clone operation
 *
 * @param uri URL to clone the repository from
 * @param callingActivity the calling activity
 */
class CloneOperation(callingActivity: ContinuationContainerActivity, uri: String) : GitOperation(callingActivity) {

  override val commands: Array<GitCommand<out Any>> =
    arrayOf(
      Git.cloneRepository().setBranch(remoteBranch).setDirectory(repository.workTree).setURI(uri),
    )
}
