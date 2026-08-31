import { Controller, Logger } from '@nestjs/common';
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
  private readonly logger = new Logger(UserController.name);

  constructor(private readonly userService: UserService) {}

  @GrpcMethod('UserService', 'CreateUser')
  async createUser(request: CreateUserRequest): Promise<UserResponse> {
    try {
      this.logger.log(`CREATE USER REQUEST: ${request.email}`);

      const result = await this.userService.createUser(
        request.email,
        request.password,
      );

      this.logger.log('CREATE USER RESULT: user created successfully');
      return result;
    } catch (error) {
      this.logger.error(
        `CREATE USER ERROR: ${
          error instanceof Error ? error.message : String(error)
        }`,
      );
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
      this.logger.error(
        `FIND USER ERROR: ${
          error instanceof Error ? error.message : String(error)
        }`,
      );
      throw error;
    }
  }
}
