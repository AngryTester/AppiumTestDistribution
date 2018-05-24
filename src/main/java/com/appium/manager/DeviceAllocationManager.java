package com.appium.manager;

import com.appium.utils.AppiumDevice;
import com.appium.utils.DevicesByHost;
import com.appium.utils.HostMachineDeviceManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yunusmete.stf.api.STFService;
import com.github.yunusmete.stf.model.DeviceBody;
import com.github.yunusmete.stf.rest.DeviceResponse;
import com.events.DeviceEventBus;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DeviceAllocationManager - Handles device initialisation, allocation and de-allocattion
 */
public class DeviceAllocationManager {

    private static DeviceAllocationManager instance;
    private static final String STF_SERVICE_URL = System.getenv("STF_URL");
    private static final String ACCESS_TOKEN = System.getenv("STF_ACCESS_TOKEN");
    static STFService service;
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
    private HostMachineDeviceManager hostMachineDeviceManager;
    private List<AppiumDevice> allDevices;

    private DeviceAllocationManager() {
        try {
            isPlatformInEnv();
            hostMachineDeviceManager = HostMachineDeviceManager.getInstance();
            ArtifactsUploader.getInstance().initializeArtifacts();
            DevicesByHost appiumDeviceByHost = hostMachineDeviceManager.getDevicesByHost();
            allDevices = appiumDeviceByHost.getAllDevices();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static DeviceAllocationManager getInstance() {
        if (instance == null) {
            instance = new DeviceAllocationManager();
        }
        return instance;
    }

    private void isPlatformInEnv() {
        if (System.getenv("Platform") == null) {
            throw new IllegalArgumentException("Please execute with Platform environment"
                    + ":: Platform=android/ios/both mvn clean -Dtest=Runner test");
        }
    }

    private void connectToSTF() {
        if (STF_SERVICE_URL != null && ACCESS_TOKEN != null) {
            connectToSTFServer();
        }
    }

    public List<AppiumDevice> getDevices() {
        LOGGER.info("All devices connected");
        return hostMachineDeviceManager.getDevicesByHost().getAllDevices();
    }

    public synchronized AppiumDevice getNextAvailableDevice() {
        int i = 0;
        for (AppiumDevice device : allDevices) {
            Thread t = Thread.currentThread();
            t.setName("Thread_" + i);
            i++;
            if (device.isAvailable()) {
                device.blockDevice();
                return device;
            }
        }
        return null;
    }

    public void freeDevice() throws JsonProcessingException {
        AppiumDeviceManager.getAppiumDevice().freeDevice();
        DeviceEventBus.getInstance().publish(new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(AppiumDeviceManager.getAppiumDevice()));
        LOGGER.info("DeAllocated Device "
                + AppiumDeviceManager.getAppiumDevice().getDevice()
                .getUdid()
                + " from execution list");
    }

    public void allocateDevice(AppiumDevice appiumDevice) throws JsonProcessingException {
        LOGGER.info("Allocated Device " + appiumDevice + " for Execution");
        AppiumDeviceManager.setDevice(appiumDevice);
        DeviceEventBus.getInstance().publish(new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(AppiumDeviceManager.getAppiumDevice()));
    }

    private void connectToSTFServer() {
        DeviceResponse devices = service.getDevices();
        List<com.github.yunusmete.stf.model.Device> deviceList = devices.getDevices();
        for (com.github.yunusmete.stf.model.Device device : deviceList) {
            if (device.isPresent()) {
                if (device.getOwner() == null) {
                    service.addDeviceToUser(new DeviceBody(device.getSerial(), 90000));
                }
            }
        }
    }
}