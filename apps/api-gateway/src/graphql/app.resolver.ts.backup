import {
  Args,
  Field,
  InputType,
  Mutation,
  ObjectType,
  Query,
  Resolver,
} from '@nestjs/graphql';
import { IsEmail, IsNotEmpty, MinLength } from 'class-validator';
import { AuthGrpcService } from '../grpc/auth.grpc.service';

@InputType()
export class SignupInput {
  @Field()
  @IsEmail()
  email!: string;

  @Field()
  @IsNotEmpty()
  @MinLength(8)
  password!: string;
}

@InputType()
export class LoginInput {
  @Field()
  @IsEmail()
  email!: string;

  @Field()
  @IsNotEmpty()
  @MinLength(8)
  password!: string;
}

@ObjectType()
export class SignupResponse {
  @Field()
  success!: boolean;

  @Field()
  message!: string;
}

@ObjectType()
export class LoginResponse {
  @Field()
  success!: boolean;

  @Field()
  message!: string;

  @Field()
  sessionToken!: string;
}

@Resolver()
export class AppResolver {
  constructor(
    private readonly authGrpcService: AuthGrpcService,
  ) {}

  @Query(() => String)
  hello(): string {
    return 'Hello from API Gateway';
  }

  @Mutation(() => SignupResponse)
  async signup(
    @Args('input') input: SignupInput,
  ): Promise<SignupResponse> {
    return this.authGrpcService.signup(
      input.email,
      input.password,
    );
  }

  @Mutation(() => LoginResponse)
  async login(
    @Args('input') input: LoginInput,
  ): Promise<LoginResponse> {
    return this.authGrpcService.login(
      input.email,
      input.password,
    );
  }
}
