import CustomButton from "../../components/CustomButton"
import DeviceInfo from "react-native-device-info"
import MessageLog from "../../components/MessageLog"
import React, { useContext, useEffect, useState } from "react"
import { BotStateContext } from "../../context/BotStateContext"
import { DeviceEventEmitter, StyleSheet, View } from "react-native"
import { MessageLogContext } from "../../context/MessageLogContext"
import { NativeModules } from "react-native"

const styles = StyleSheet.create({
    root: {
        flex: 1,
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        marginHorizontal: 10,
    },
})

const Home = () => {
    const { StartModule } = NativeModules

    const [isRunning, setIsRunning] = useState<boolean>(false)

    const bsc = useContext(BotStateContext)
    const mlc = useContext(MessageLogContext)

    useEffect(() => {
        DeviceEventEmitter.addListener("MediaProjectionService", (data) => {
            setIsRunning(data["message"] === "Running")
        })

        DeviceEventEmitter.addListener("BotService", (data) => {
            if (data["message"] === "Running") {
                mlc.setAsyncMessages([])
                mlc.setMessageLog([])
            }
        })

        getVersion()
    }, [])

    // Grab the program version.
    const getVersion = () => {
        console.log("Android app version is ", DeviceInfo.getVersion())
        bsc.setAppVersion(DeviceInfo.getVersion())
    }

    return (
        <View style={styles.root}>
            {isRunning ? (
                <CustomButton title="Stop" backgroundColor="red" width={200} borderRadius={20} onPress={() => StartModule.stop()} />
            ) : (
                <CustomButton disabled={!bsc.readyStatus} title={bsc.readyStatus ? "Start" : "Not Ready"} width={200} borderRadius={20} onPress={() => StartModule.start()} />
            )}

            <MessageLog />
        </View>
    )
}

export default Home
