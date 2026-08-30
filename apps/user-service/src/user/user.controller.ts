import { Controller } from '@nestjs/common';
import { GrpcMethod } from '@nestjs/microservices';
import { UserService, UserResponse } from './user.service';

interface CreateUserRequest {
  email: string;
  password: string;
}

interface FindUserByEmailRequest {
  email: string;
}

@Controller()
export class UserController {
  constructor(private readonly userService: UserService) {}

  @GrpcMethod('UserService', 'CreateUser')
  async createUser(request: CreateUserRequest): Promise<UserResponse> {
    try {
      console.log('CREATE USER REQUEST:', request);
      const result = await this.userService.createUser(
        request.email,
        request.password,
      );
      console.log('CREATE USER RESULT:', result);
      return result;
    } catch (error) {
      console.error('CREATE USER ERROR:', error);
      throw error;
    }
  }

  @GrpcMethod('UserService', 'FindUserByEmail')
  async findUserByEmail(
    request: FindUserByEmailRequest,
  ): Promise<UserResponse> {
    try {
      return await this.userService.findUserByEmail(request.email);
    } catch (error) {
      console.error('FIND USER ERROR:', error);
      throw error;
    }
  }
}
