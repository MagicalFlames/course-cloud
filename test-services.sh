#!/bin/bash

echo "=== 测试微服务拆分 ==="

# 1. 测试课程目录服务 - 创建课程
echo -e "\n1. 测试课程目录服务 - 创建课程"
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -d '{
  "code": "CS101",
  "title": "计算机科学导论",
  "instructor": {
    "id": "T001",
    "name": "张教授",
    "email": "zhang@example.edu.cn"
  },
  "scheduleSlot": {
    "dayOfWeek": "MONDAY",
    "startTime": "08:00",
    "endTime": "10:00",
    "expectedAttendance": 50
  },
  "capacity": 60,
  "enrolledCount": 0
}'

# 2. 获取所有课程
echo -e "\n\n2. 获取所有课程"
curl http://localhost:8081/api/courses

# 3. 测试选课服务 - 创建学生
echo -e "\n\n3. 测试选课服务 - 创建学生"
curl -X POST http://localhost:8082/api/students \
  -H "Content-Type: application/json" \
  -d '{
  "studentId": "2024001",
  "name": "张三",
  "major": "计算机科学与技术",
  "grade": 2024,
  "email": "zhangsan@example.edu.cn"
}'

# 4. 获取所有学生
echo -e "\n\n4. 获取所有学生"
curl http://localhost:8082/api/students

# 5. 测试选课（验证服务间通信）
echo -e "\n\n5. 测试学生选课（服务间通信）"
sleep 2
COURSE_ID=$(curl -s http://localhost:8081/api/courses | jq -r '.message.data[0].id')
echo "课程ID: $COURSE_ID"
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d "{
  \"courseId\": \"$COURSE_ID\",
  \"studentId\": \"2024001\"
}"

# 6. 查询选课记录
echo -e "\n\n6. 查询选课记录"
curl http://localhost:8082/api/enrollments

# 7. 测试课程不存在的情况
echo -e "\n\n7. 测试选课失败（课程不存在）"
curl -X POST http://localhost:8082/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
  "courseId": "non-existent-course",
  "studentId": "2024001"
}'

echo -e "\n\n=== 测试完成 ==="
