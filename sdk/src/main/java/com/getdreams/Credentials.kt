/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams

/**
 * Class containing the user's credentials.
 *
 * @param idToken The token used to authenticate the user.
 */
data class Credentials(val idToken: String)
