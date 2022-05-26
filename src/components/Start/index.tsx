import RNFS from "react-native-fs"
import { BotStateContext, defaultSettings, Settings } from "../../context/BotStateContext"
import { MessageLogContext } from "../../context/MessageLogContext"
import { useContext, useEffect, useState } from "react"

const Start = () => {
    const [firstTime, setFirstTime] = useState<boolean>(true)

    const bsc = useContext(BotStateContext)
    const mlc = useContext(MessageLogContext)

    // Load settings if local settings.json file exists in internal storage.
    useEffect(() => {
        loadSettings()
    }, [])

    useEffect(() => {
        saveSettings()
    }, [bsc.settings])

    useEffect(() => {
        if (mlc.asyncMessages.length > 0) {
            const newLog = [...mlc.messageLog, ...mlc.asyncMessages]
            mlc.setMessageLog(newLog)
        }
    }, [mlc.asyncMessages])

    const saveSettings = async (newSettings?: Settings) => {
        if (!firstTime) {
            // Grab a local copy of the current settings.
            const localSettings: Settings = newSettings ? newSettings : bsc.settings

            // Save settings to local settings.json file in internal storage.
            const path = RNFS.ExternalDirectoryPath + "/settings.json"

            let toSave = JSON.stringify(localSettings, null, 4)

            // Delete settings.json file first as RNFS.writeFile() does not clear the file first before writing on top of it.
            // This is the reason why there are extra brackets and fields "appended" to the end of the file before.
            // Source: https://github.com/itinance/react-native-fs/issues/869#issuecomment-602067100
            // Note: It unfortunately still happens.
            await RNFS.unlink(path)
                .then(() => {
                    console.log("settings.json file successfully deleted.")
                })
                .catch(() => {
                    console.log("settings.json file does not exist so no need to delete it before saving current settings.")
                })

            await RNFS.writeFile(path, toSave)
                .then(() => {
                    console.log("Settings saved to ", path)
                    mlc.setAsyncMessages([])
                    mlc.setMessageLog([`\n[SUCCESS] Settings saved to ${path}`])
                })
                .catch((e) => {
                    console.error(`Error writing settings to path ${path}: ${e}`)
                    mlc.setMessageLog([...mlc.messageLog, `\n[ERROR] Error writing settings to path ${path}: \n${e}`])
                })
                .finally(() => {
                    handleReady()
                })
        } else {
            // Perform ready check upon loading up the settings on a cold boot.
            handleReady()
        }
    }

    const loadSettings = async () => {
        const path = RNFS.ExternalDirectoryPath + "/settings.json"
        let newSettings: Settings = defaultSettings
        let normalData = ""
        let corruptionFixed = false
        await RNFS.readFile(path)
            .then(async (data) => {
                console.log(`Loaded settings from settings.json file.`)
                normalData = data

                const parsed: Settings = JSON.parse(data)
                const fixedSettings: Settings = fixSettings(parsed)
                newSettings = fixedSettings
            })
            .catch((e: Error) => {
                if (e.name === "SyntaxError") {
                    // If file corruption occurred, attempt to fix by removing the offending characters one by one from the JSON string.
                    let fixedData = normalData
                    while (true) {
                        fixedData = fixedData.substring(0, fixedData.length - 1)
                        try {
                            const parsed: Settings = JSON.parse(fixedData)
                            const fixedSettings: Settings = fixSettings(parsed)
                            newSettings = fixedSettings
                            corruptionFixed = true
                        } catch {}

                        if (corruptionFixed || fixedData.length === 0) {
                            break
                        }
                    }

                    console.log("Finished attempting to fix corruption.")
                    if (corruptionFixed) {
                        console.log("Automatic fix was successful!")
                    } else {
                        console.error(`Error reading settings from path ${path}: ${e.name}`)
                        mlc.setMessageLog([
                            ...mlc.messageLog,
                            `\n[ERROR] Error reading settings from path ${path}: \n${e}`,
                            "\nNote that the application sometimes corrupts the settings.json when saving. Automatic fix was not successful.",
                        ])
                    }
                } else if (!e.message.includes("No such file or directory")) {
                    console.error(`Error reading settings from path ${path}: ${e.name}`)
                    mlc.setMessageLog([...mlc.messageLog, `\n[ERROR] Error reading settings from path ${path}: \n${e}`])
                }
            })
            .finally(() => {
                console.log("Read: " + JSON.stringify(newSettings, null, 4))
                bsc.setSettings(newSettings)
                setFirstTime(false)
            })
    }

    // Attempt to fix missing key-value pairs in the settings before commiting them to state.
    const fixSettings = (decoded: Settings) => {
        var newSettings: Settings = decoded
        Object.keys(defaultSettings).forEach((key) => {
            if (decoded[key as keyof Settings] === undefined) {
                newSettings = {
                    ...newSettings,
                    [key as keyof Settings]: defaultSettings[key as keyof Settings],
                }
            }
        })

        return newSettings
    }

    // Determine whether the program is ready to start.
    const handleReady = () => {
        bsc.setReadyStatus(bsc.settings.property1)
    }

    return null
}

export default Start
