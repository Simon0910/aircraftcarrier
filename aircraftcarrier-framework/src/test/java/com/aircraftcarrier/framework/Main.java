package com.aircraftcarrier.framework;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2025/3/20
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) {
        // 1,2,3,3,3,5,6
        // int[] arr = {1,2,3,3,3,5,6};
        // int index = fun(arr, 4);
        // System.out.println(index);

        int[] arr = {1, 7, 7, 7, 9, 9, 11, 13};
        int target = 8;
        int result = binarySearchLast(arr, target);
        if (result != -1) {
            System.out.println("目标数 " + target + " 的索引位置是: " + result);
        } else {
            System.out.println("目标数 " + target + " 不在数组中。");
        }
    }

    public static int binarySearchLast(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        int result = -1;
        int j  = -1;

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

    public static int binarySearch(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) {
                return mid;
            } else if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }


    public static int fun(int[] nums, int target) {
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
