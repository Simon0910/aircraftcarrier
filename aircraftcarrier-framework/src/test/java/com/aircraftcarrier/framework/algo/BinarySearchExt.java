package com.aircraftcarrier.framework.algo;

/**
 * 如果有序数组里有重复的，找到最后一个出现的位置，未找到返回应该的位置
 *
 * @author zhipengliu
 * @date 2025/3/20
 * @since 1.0
 */
public class BinarySearchExt {

    public static void main(String[] args) {
        /**
         [1,3,5,6]
         5
         [1,3,5,6]
         2
         [1,3,5,6]
         7
         [1,3,5,6]
         0
         [1,3,5,5,6]
         5
         */

        int[] arr = {1, 3, 5, 5, 5, 6};
        int target = 7;

        int result = binarySearchFirstPosition(arr, target);
        if (result != -1) {
            System.out.println("目标数 " + target + " 的索引位置是: " + result);
        } else {
            System.out.println("目标数 " + target + " 不在数组中。");
        }
    }

    /**
     * 如果有序数组里有重复的，找到最后一个出现的位置，未找到返回应该的位置
     */
    public static int binarySearchLastPosition2(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        int result = -1;

        while (left <= right) {
            int mid = left + ((right - left) >> 1);

            if (nums[mid] <= target) {
                result = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        // if (result == -1) {
        //     if (target < nums[0]) {
        //         return 0;
        //     }
        //     if (target >= nums[nums.length - 1]) {
        //         return nums.length;
        //     }
        // }
        // return nums[result] == target ? result : right;

        // 检查 right 是否指向目标值
        if (right >= 0 && right < nums.length && nums[right] == target) {
            return right;
        } else {
            // 未找到时返回应插入的位置（right + 1）
            return right + 1;
        }
    }

    /**
     * 如果有序数组里有重复的，找到最后出现的位置，未找到返回应该的位置
     */
    public static int binarySearchLastPosition(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        int result = -1;
        int j = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) {
                result = mid;
                left = mid + 1;
            } else if (arr[mid] < target) {
                j = mid;
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return result != -1 ? result : ++j;
    }

    /**
     * 如果有序数组里有重复的，找到第一个出现的位置，未找到返回应该的位置
     */
    public static int binarySearchFirstPosition(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;
        int result = nums.length;

        while (left <= right) {
            int mid = left + ((right - left) >> 1);

            if (nums[mid] < target) {
                left = mid + 1;
            } else {
                result = mid;
                right = mid - 1;
            }
        }
        // return result;

        return right > -1 && nums[right] == target ? right : left;
    }

    /**
     * 二分查找
     */
    // public static int binarySearch(int[] arr, int target) {
    //     int left = 0;
    //     int right = arr.length - 1;
    //     while (left <= right) {
    //         int mid = left + (right - left) / 2;
    //         if (arr[mid] == target) {
    //             return mid;
    //         } else if (arr[mid] < target) {
    //             left = mid + 1;
    //         } else {
    //             right = mid - 1;
    //         }
    //     }
    //     return -1;
    // }

    /**
     * 如果有序数组里有重复的，找到最后出现的位置，未找到返回应该的位置
     */
    public static int searchLastPosition(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        int result = -1;
        int j = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == target) {
                result = i;
            } else if (nums[i] < target) {
                j = i;
            }
        }

        return result != -1 ? result : ++j;
    }


}
