/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.migrationtool;


import nl.xillio.migrationtool.dialogs.AddLicenseDialog;
import nl.xillio.xill.util.settings.SettingsHandler;

/**
 * This class is responsible for performing a license (EULA) check.
 */
public class EulaUtils {
    public static final String EULA_LOCATION = "https://support.xillio.com/support/solutions/articles/6000110301-xill-ide-3-end-user-license-agreement";

    static {
        SettingsHandler.getSettingsHandler().simple().register("EULA", "eulaAccepted", "false", "Whether the End User License Agreement (EULA) was accepted by the user.");
    }

    /**
     * Show a license dialog if the EULA has not been accepted yet and check whether the user accepts the EULA
     *
     * @param insist Ask for a confirmation of the EULA regardless of whether this was already accepted
     */
    public static boolean performEulaCheck(boolean insist) {

        if (!insist && eulaAccepted()) {
            return true;
        }

        AddLicenseDialog dialog = new AddLicenseDialog();
        dialog.showAndWait();

        boolean accepted = dialog.getEulaChoice();

        SettingsHandler.getSettingsHandler().simple().save("EULA", "eulaAccepted", accepted);

        return eulaAccepted();
    }


    /**
     * Return true if and only if the EULA has previously been accepted by the user.
     *
     */
    private static boolean eulaAccepted() {
        return SettingsHandler.getSettingsHandler().simple().getBoolean("EULA", "eulaAccepted");
    }

}
