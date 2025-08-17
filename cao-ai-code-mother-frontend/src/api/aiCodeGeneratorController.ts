// @ts-ignore
/* eslint-disable */
// AI代码生成相关接口

import request from '@/request'

/** 生成代码 */
export async function generateCode(body: string, options?: { [key: string]: any }) {
  return request.post<string>('/ai/code/generate', body, options);
}