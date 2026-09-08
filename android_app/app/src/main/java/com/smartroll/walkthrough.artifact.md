# SmartRoll: BLE Data (ID) Implementation

## Summary of Changes
1. **Service Data (ID) Approach**: Replaced simple role-based byte advertising with `ServiceData` injection using `addServiceData(DATA_UUID, userId.toByteArray())`. This uniquely identifies users while minimizing overhead.
2. **BleManager Optimization**:
    - Removed `ROLE_TEACHER`/`ROLE_STUDENT` byte constants.
    - Simplified `startAdvertising` to accept `userId`.
    - Added fast-start initialization (200ms delay vs 2s) to reduce latency.
3. **DashboardActivity Integration**:
    - Updated `startBleAction` to broadcast `user.id` (prefixing with `tea_` or `stu_` for role identification).
    - Updated `onDeviceFound` to parse the `userId` string, replacing reliance on static role bytes.
    - Maintained the 5s attendance marking cooldown to prevent spam.

## Verification
- BLE detection now identifies the specific user rather than just a role type.
- Robust cleanup is handled by explicit `stopAdvertising`/`stopScan` calls, preventing BLE "Error 1".
